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

/** Resolves Mycoid Resurrection's graveyard-wide perpetual boost. */
@Component
public class PerpetuallyBoostCreatureCardsInGraveyardEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyBoostCreatureCardsInGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        if (graveyard == null || graveyard.isEmpty()) {
            return;
        }

        int permanentCount = (int) graveyard.stream()
                .filter(this::isPermanentCard)
                .count();
        for (Card card : graveyard) {
            if (card != null && card.hasType(CardType.CREATURE)) {
                PerpetualCardPowerToughnessSupport.remember(
                        gameData, card, permanentCount, permanentCount);
            }
        }
    }

    private boolean isPermanentCard(Card card) {
        return card != null && !card.isToken()
                && ((card.getType() != null && card.getType().isPermanentType())
                || card.getAdditionalTypes().stream().anyMatch(CardType::isPermanentType));
    }
}
