package com.stastnyjakub.interlos.Services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import com.stastnyjakub.interlos.Model.Labyrinth;
import com.stastnyjakub.interlos.Model.Labyrinth.StepResult;
import com.stastnyjakub.interlos.Services.LanguageService.Instruction.InstructionType;

public class LanguageService {

    public static class Instruction {
        public enum InstructionType {
            FORWARD, BACKWARD, LEFT, RIGHT, JUMP_IF_WALL, JUMP_IF_NOT_WALL, JUMP
        }

        private final InstructionType instruction;
        private final Integer argument;

        public Instruction(InstructionType instruction, Integer argument) {
            this.instruction = instruction;
            this.argument = argument;
        }

        public static Boolean isSimple(char ch) {
            return "<>+-".indexOf(ch) >= 0;
        }

        public static Instruction simpleFromCharacter(char ch) {
            if (ch == '<')
                return new Instruction(InstructionType.LEFT);
            if (ch == '>')
                return new Instruction(InstructionType.RIGHT);
            if (ch == '+')
                return new Instruction(InstructionType.FORWARD);
            if (ch == '-')
                return new Instruction(InstructionType.BACKWARD);
            throw new IllegalArgumentException();
        }

        public Instruction(InstructionType instruction) {
            this(instruction, null);
        }

        public InstructionType getInstruction() {
            return this.instruction;
        }

        public Integer getArgument() {
            return this.argument;
        }
    }

    public enum Result {
        GOAL_REACHED, WALL_HIT, TERMINATED_AFTER_LIMIT, PROGRAM_ENDED
    }

    public enum SyntaxResult {
        VALID, NO_JUMP_GOAL, ENDS_WITH_CONDITION, MORE_CONDITIONS_IN_ROW, UNNECESSARY_JUMP_GOAL, UNCERTAIN_JUMP_GOAL
    }

    private static final String ALLOWED_CHARS = "<>+-?!abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static Boolean validateAllowedSymbols(String code) {
        if (code == null)
            return false;
        for (char ch : code.toCharArray())
            if (ALLOWED_CHARS.indexOf(ch) == -1)
                return false;
        return true;
    }

    public static boolean detectBruteForce(String code) {
        if (code == null)
            return false;
        return code.length() > 32;
    }

    public static SyntaxResult validateSyntax(String code) {
        Set<Character> usedLetters = new HashSet<>();
        boolean lastCondJump = false;
        for (char ch : code.toCharArray()) {
            if (Character.isAlphabetic(ch)) {
                if (Character.isLowerCase(ch) && usedLetters.contains(ch))
                    return SyntaxResult.UNCERTAIN_JUMP_GOAL;
                usedLetters.add(ch);
            }
            if (ch == '?' || ch == '!') {
                if (lastCondJump)
                    return SyntaxResult.MORE_CONDITIONS_IN_ROW;
                lastCondJump = true;
            } else {
                lastCondJump = false;
            }
        }
        if (lastCondJump)
            return SyntaxResult.ENDS_WITH_CONDITION;
        for (char ch : usedLetters) {
            if (Character.isUpperCase(ch) && !usedLetters.contains(Character.toLowerCase(ch)))
                return SyntaxResult.NO_JUMP_GOAL;
        }
        for (char ch : usedLetters) {
            if (Character.isLowerCase(ch) && !usedLetters.contains(Character.toUpperCase(ch)))
                return SyntaxResult.UNNECESSARY_JUMP_GOAL;
        }
        return SyntaxResult.VALID;
    }

    public static Instruction[] compile(String code) {
        List<Instruction> res = new ArrayList<>();
        Map<Character, Integer> lowerLettersPositions = new HashMap<>();
        Queue<Character> upperLettersOrder = new LinkedList<>();

        for (int i = 0; i < code.length(); i++) {
            char current = code.charAt(i);
            if (Instruction.isSimple(current))
                res.add(Instruction.simpleFromCharacter(current));
            else if (current == '?')
                res.add(new Instruction(InstructionType.JUMP_IF_NOT_WALL, res.size() + 2));
            else if (current == '!')
                res.add(new Instruction(InstructionType.JUMP_IF_WALL, res.size() + 2));
            else {
                if (Character.isLowerCase(current))
                    lowerLettersPositions.put(current, res.size());
                else {
                    upperLettersOrder.add(current);
                    res.add(null);
                }
            }
        }
        for (int i = 0; i < res.size(); i++) {
            if (res.get(i) == null) {
                Character letter = upperLettersOrder.remove();
                Integer pos = lowerLettersPositions.get(Character.toLowerCase(letter));
                res.set(i, new Instruction(InstructionType.JUMP, pos));
            }
        }
        return res.toArray(Instruction[]::new);
    }

    public static Result run(Instruction[] program, Labyrinth environment, int limit) {
        int instructionPointer = 0;
        for (int steps = 0; steps < limit; steps++) {
            if (instructionPointer >= program.length)
                return Result.PROGRAM_ENDED;

            InstructionType current = program[instructionPointer].getInstruction();

            if (current == InstructionType.FORWARD) {
                StepResult res = environment.stepForward();
                if (res == StepResult.GOAL_REACHED)
                    return Result.GOAL_REACHED;
                if (res == StepResult.WALL_HIT)
                    return Result.WALL_HIT;
            } else if (current == InstructionType.BACKWARD) {
                StepResult res = environment.stepBackward();
                if (res == StepResult.GOAL_REACHED)
                    return Result.GOAL_REACHED;
                if (res == StepResult.WALL_HIT)
                    return Result.WALL_HIT;
            } else if (current == InstructionType.LEFT) {
                environment.turnLeft();
            } else if (current == InstructionType.RIGHT) {
                environment.turnRight();
            } else if (current == InstructionType.JUMP) {
                instructionPointer = program[instructionPointer].getArgument() - 1;
            } else if (current == InstructionType.JUMP_IF_WALL) {
                if (environment.isWallBeforeMe())
                    instructionPointer = program[instructionPointer].getArgument() - 1;
            } else if (current == InstructionType.JUMP_IF_NOT_WALL) {
                if (!environment.isWallBeforeMe())
                    instructionPointer = program[instructionPointer].getArgument() - 1;
            }
            instructionPointer++;
        }
        return Result.TERMINATED_AFTER_LIMIT;
    }
}
