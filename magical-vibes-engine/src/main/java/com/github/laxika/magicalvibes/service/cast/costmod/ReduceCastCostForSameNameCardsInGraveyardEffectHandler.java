package com.github.laxika.magicalvibes.service.cast.costmod;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForSameNameCardsInGraveyardEffect;
import com.github.laxika.magicalvibes.service.cast.CostModificationContext;
import com.github.laxika.magicalvibes.service.cast.CostModificationHandlerBean;
import com.github.laxika.magicalvibes.service.cast.CostModificationSource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class ReduceCastCostForSameNameCardsInGraveyardEffectHandler implements CostModificationHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReduceCastCostForSameNameCardsInGraveyardEffect.class;
    }

    @Override
    public int modifyCost(CostModificationContext context, CardEffect effect, CostModificationSource source) {
        if (!source.controlledBy(context.castingPlayerId()) || context.spell() == null) {
            return 0;
        }
        List<Card> graveyard = context.gameData().playerGraveyards.get(context.castingPlayerId());
        if (graveyard == null) {
            return 0;
        }
        int matchingCards = 0;
        for (Card card : graveyard) {
            if (!card.isToken()
                    && !Objects.equals(card.getId(), context.spell().getId())
                    && Objects.equals(card.getName(), context.spell().getName())) {
                matchingCards++;
            }
        }
        return -matchingCards;
    }
}
