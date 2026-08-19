package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentLosesLifeEqualToCardsInTheirGraveyardEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves Angrath's graveyard-sized life loss separately for each opponent. */
@Component
@RequiredArgsConstructor
public class EachOpponentLosesLifeEqualToCardsInTheirGraveyardEffectHandler
        implements NormalEffectHandlerBean {

    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentLosesLifeEqualToCardsInTheirGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        if (controllerId == null) {
            return;
        }

        String sourceName = entry.getCard().getName();
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(controllerId)) {
                continue;
            }
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            int cardsInGraveyard = graveyard == null
                    ? 0
                    : (int) graveyard.stream().filter(card -> !card.isToken()).count();
            if (cardsInGraveyard > 0) {
                lifeSupport.applyLifeLoss(gameData, playerId, cardsInGraveyard, sourceName);
            }
        }
    }
}
