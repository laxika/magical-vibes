package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerWithMostCardsInHandGainsControlOfSourceCreatureEffect;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlayerWithMostCardsInHandGainsControlOfSourceCreatureEffectHandler implements NormalEffectHandlerBean {

    private final CreatureControlService creatureControlService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PlayerWithMostCardsInHandGainsControlOfSourceCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = findSource(gameData, entry.getCard().getId());
        if (source == null) {
            return;
        }

        int highestHandSize = -1;
        UUID playerWithMostCards = null;
        boolean tiedForMostCards = false;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> hand = gameData.playerHands.get(playerId);
            int handSize = hand == null ? 0 : hand.size();
            if (handSize > highestHandSize) {
                highestHandSize = handSize;
                playerWithMostCards = playerId;
                tiedForMostCards = false;
            } else if (handSize == highestHandSize) {
                tiedForMostCards = true;
            }
        }

        if (tiedForMostCards || playerWithMostCards == null) {
            return;
        }

        creatureControlService.applyControlEffect(gameData, playerWithMostCards, source,
                new GainControlOfTargetEffect(ControlDuration.PERMANENT),
                EffectDuration.PERMANENT, null, entry.getCard().getName());
    }

    private Permanent findSource(GameData gameData, UUID cardId) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : battlefield) {
                if (permanent.getCard().getId().equals(cardId)) {
                    return permanent;
                }
            }
        }
        return null;
    }
}
