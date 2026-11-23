package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.NoteAmountEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Resolves {@link NoteAmountEffect}: evaluates the amount once against the resolving entry, its source as it is or
 * last existed (CR 608.2h), and records it as the {@link StepKind#NOTE} step. It never prompts and changes nothing in
 * the game, so it logs nothing.
 */
@Component
@RequiredArgsConstructor
public class NoteAmountEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return NoteAmountEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var note = (NoteAmountEffect) effect;
        var source = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        int amount = amountEvaluationService.evaluate(gameData, note.amount(), AmountContext.forStackEntry(entry, source));
        entry.recordStep(StepKind.NOTE, StepOutcome.ofAmount(amount, List.of()));
    }
}
