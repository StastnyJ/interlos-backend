package com.stastnyjakub.interlos.Controllers;

import com.stastnyjakub.interlos.Model.Labyrinth;
import com.stastnyjakub.interlos.Services.LanguageService;
import com.stastnyjakub.interlos.Services.LanguageService.Instruction;
import com.stastnyjakub.interlos.Services.LanguageService.Result;
import com.stastnyjakub.interlos.Services.LanguageService.SyntaxResult;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
public class GameController {

    private final int INSTRUCTIONS_LIMIT = 10000;
    private final String PASSWORD = "JD8ATZ67VGASOC90AS";

    @CrossOrigin
    @GetMapping("/ping")
    public String ping() {
        return "PING Game controller";
    }

    @CrossOrigin
    @PostMapping("/eval")
    public String evaluate(@RequestBody String code) {
        if (code == null || code.length() == 0)
            return "Nenašel jsi cestu ven.\n";

        if (code.charAt(code.length() - 1) == '=')
            code = code.substring(0, code.length() - 1);

        if (!LanguageService.validateAllowedSymbols(code))
            return "Tomuto nerozumím, povolené znaky jsou pouze: < > + - ? ! a-z A-Z.\n";
        if (LanguageService.detectBruteForce(code))
            return "Jseš nějak moc ukecanej.\n";
        SyntaxResult syntaxRes = LanguageService.validateSyntax(code);
        if (syntaxRes == SyntaxResult.NO_JUMP_GOAL)
            return "Zkontroluj si, že máš vždy kam skákat.\n";
        if (syntaxRes == SyntaxResult.ENDS_WITH_CONDITION)
            return "Nakonci ti ještě něco chybí.\n";
        if (syntaxRes == SyntaxResult.MORE_CONDITIONS_IN_ROW)
            return "Ne všechny znaky mohou být za sebou v řadě.\n";
        if (syntaxRes == SyntaxResult.UNCERTAIN_JUMP_GOAL)
            return "Některé znaky můžeš použít pouze jednou.\n";
        Instruction[] program = LanguageService.compile(code);
        Labyrinth labyrinth = Labyrinth.getTaskLabyrinth();
        Result res = LanguageService.run(program, labyrinth, INSTRUCTIONS_LIMIT);
        if (res == Result.GOAL_REACHED)
            return "Gratuluji, dostal jsi se z labyrintu. Heslo je: " + PASSWORD + ".\n";
        else if (res == Result.PROGRAM_ENDED) {
            if (syntaxRes == SyntaxResult.UNNECESSARY_JUMP_GOAL)
                return "Některé znaky jsou nadbytečné a ignorovány.\n";
            else
                return "Ještě nejsi venku.\n";
        } else if (res == Result.TERMINATED_AFTER_LIMIT)
            return "Chodíš stále v kruzích, zkus to jinak.\n";
        else if (res == Result.WALL_HIT)
            return "Narazil jsi do zdi.\n";

        throw new IllegalStateException();
    }
}
