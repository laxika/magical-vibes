package com.github.laxika.magicalvibes.service.effect.normalfx;
import com.github.laxika.magicalvibes.model.action.DelayedGraveyardToHandReturn;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnCardFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import java.util.List;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterDelayedReturnCardFromGraveyardToHandEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedReturnCardFromGraveyardToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RegisterDelayedReturnCardFromGraveyardToHandEffect) effect;

        UUID cardId = e.cardId();
        if (cardId == null) return;

        // Find the card's owner (whose graveyard it's in)
        UUID ownerId = null;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard != null && graveyard.stream().anyMatch(c -> c.getId().equals(cardId))) {
                ownerId = playerId;
                break;
            }
        }

        if (ownerId == null) {
            String logEntry = "Delayed return fizzles — card is no longer in any graveyard.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - Delayed return registered but card {} not in any graveyard", gameData.id, cardId);
            return;
        }

        gameData.queueDelayedAction(new DelayedGraveyardToHandReturn(cardId, ownerId));
        log.info("Game {} - Delayed graveyard-to-hand return registered for card {} (owner {})",
                gameData.id, cardId, ownerId);
    }
}
