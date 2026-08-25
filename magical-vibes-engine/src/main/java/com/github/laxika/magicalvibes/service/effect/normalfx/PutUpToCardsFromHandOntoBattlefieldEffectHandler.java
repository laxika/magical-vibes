package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutUpToCardsFromHandOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PutUpToCardsFromHandOntoBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final PutUpToCardsFromHandOntoBattlefieldSupport support;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutUpToCardsFromHandOntoBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        PutUpToCardsFromHandOntoBattlefieldEffect putEffect =
                (PutUpToCardsFromHandOntoBattlefieldEffect) effect;
        int maxCount = Math.max(0, amountEvaluationService.evaluate(gameData, putEffect.maxCount(),
                AmountContext.forStackEntry(entry, null)));
        support.beginChoice(gameData, entry.getControllerId(), putEffect.predicate(), putEffect.label(),
                maxCount, entry.getCard().getId(), entry.getCard().getName(), putEffect.tapped());
    }
}
