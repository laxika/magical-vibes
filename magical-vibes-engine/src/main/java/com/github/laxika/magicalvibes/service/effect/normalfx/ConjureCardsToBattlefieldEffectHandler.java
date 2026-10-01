package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardsToBattlefieldEffect;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** Resolves a dynamic number of named battlefield conjures. */
@Component
@RequiredArgsConstructor
public class ConjureCardsToBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureCardsToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ConjureCardsToBattlefieldEffect conjure = (ConjureCardsToBattlefieldEffect) effect;
        int count = amountEvaluationService.evaluate(
                gameData, conjure.count(), AmountContext.forStackEntry(entry, null));
        if (count <= 0) {
            return;
        }

        List<CardEffect> conjures = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            conjures.add(new ConjureCardToBattlefieldEffect(conjure.cardName()));
        }

        int index = entry.getEffectsToResolve().indexOf(effect);
        if (index < 0) {
            throw new IllegalStateException("Current effect is not present in its stack entry");
        }
        entry.insertEffectsToResolve(index + 1, conjures);
    }
}
