package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceCardEffect;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves counter placement on the source card's battlefield permanent. */
@Component
@RequiredArgsConstructor
public class PutCountersOnSourceCardEffectHandler implements NormalEffectHandlerBean {

    private final PermanentCounterSupport permanentCounterSupport;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCountersOnSourceCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Card sourceCard = entry.getCard();
        if (sourceCard == null) {
            return;
        }

        Permanent source = findSourcePermanent(gameData, sourceCard);
        if (source != null) {
            PutCountersOnSourceCardEffect counters = (PutCountersOnSourceCardEffect) effect;
            int count = counters.amount() == null
                    ? counters.count()
                    : amountEvaluationService.evaluate(gameData, counters.amount(),
                    AmountContext.forStackEntry(entry, source));
            permanentCounterSupport.placeCounterOnPermanent(
                    gameData, entry, source, counters.counterType(), count);
        }
    }

    private Permanent findSourcePermanent(GameData gameData, Card sourceCard) {
        for (var battlefield : gameData.playerBattlefields.values()) {
            for (Permanent permanent : battlefield) {
                if (permanent.getCard().getId().equals(sourceCard.getId())
                        || permanent.getOriginalCard().getId().equals(sourceCard.getId())) {
                    return permanent;
                }
            }
        }
        return null;
    }
}
