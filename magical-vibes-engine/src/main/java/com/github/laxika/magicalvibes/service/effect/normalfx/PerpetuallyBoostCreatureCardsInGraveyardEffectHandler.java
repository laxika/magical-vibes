package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostCreatureCardsInGraveyardEffect;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves a perpetual power/toughness boost for creature cards in a graveyard. */
@Component
public class PerpetuallyBoostCreatureCardsInGraveyardEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyBoostCreatureCardsInGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var boost = (PerpetuallyBoostCreatureCardsInGraveyardEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        if (graveyard == null) {
            return;
        }

        for (Card card : graveyard) {
            if (card.hasType(CardType.CREATURE)) {
                PerpetualCardPowerToughnessSupport.remember(
                        gameData, card, boost.powerBoost(), boost.toughnessBoost());
            }
        }
    }
}
