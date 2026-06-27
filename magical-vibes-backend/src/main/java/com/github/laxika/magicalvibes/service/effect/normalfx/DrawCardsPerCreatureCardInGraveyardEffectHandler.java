package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardsPerCreatureCardInGraveyardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DrawCardsPerCreatureCardInGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DrawCardsPerCreatureCardInGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DrawCardsPerCreatureCardInGraveyardEffect) effect;

        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);
        String cardName = entry.getCard().getName();

        int creatureCount = 0;
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        if (graveyard != null) {
            for (Card card : graveyard) {
                if (card.hasType(CardType.CREATURE)) {
                    creatureCount++;
                }
            }
        }

        int drawCount = creatureCount * e.cardsPerCreature();
        if (drawCount <= 0) {
            String logEntry = playerName + " draws 0 cards from " + cardName + " (no creature cards in graveyard).";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} draws 0 from {} (no creature cards in graveyard)", gameData.id, playerName, cardName);
            return;
        }

        playerInteractionSupport.applyDrawCards(gameData, controllerId, drawCount);

        String logEntry = playerName + " draws " + drawCount + " card" + (drawCount != 1 ? "s" : "")
                + " from " + cardName + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} draws {} from {}", gameData.id, playerName, drawCount, cardName);
    
    }
}
