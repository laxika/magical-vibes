package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutUpToCardsFromHandOrGraveyardOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PutUpToCardsFromHandOrGraveyardOntoBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final PutUpToCardsFromHandOntoBattlefieldSupport support;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutUpToCardsFromHandOrGraveyardOntoBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        PutUpToCardsFromHandOrGraveyardOntoBattlefieldEffect putEffect =
                (PutUpToCardsFromHandOrGraveyardOntoBattlefieldEffect) effect;
        int maxCount = Math.max(0, amountEvaluationService.evaluate(gameData, putEffect.maxCount(),
                AmountContext.forStackEntry(entry, null)));
        support.beginChoice(gameData, entry.getControllerId(), putEffect.predicate(), putEffect.label(),
                maxCount, entry.getCard().getId(), entry.getCard().getName(), true, true);
    }
}
