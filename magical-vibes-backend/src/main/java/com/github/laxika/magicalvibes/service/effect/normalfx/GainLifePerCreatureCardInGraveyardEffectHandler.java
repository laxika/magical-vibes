package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifePerCreatureCardInGraveyardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class GainLifePerCreatureCardInGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final LifeSupport lifeSupport;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainLifePerCreatureCardInGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (GainLifePerCreatureCardInGraveyardEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        int creatureCount = 0;
        if (graveyard != null) {
            for (Card card : graveyard) {
                if (card.hasType(CardType.CREATURE)) {
                    creatureCount++;
                }
            }
        }
        if (creatureCount == 0) {
            String playerName = gameData.playerIdToName.get(controllerId);
            String logEntry = playerName + " has no creature cards in their graveyard.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} has no creature cards in graveyard for life gain", gameData.id, playerName);
            return;
        }
        lifeSupport.applyGainLife(gameData, controllerId, creatureCount * e.lifePerCreature());
    }
}
