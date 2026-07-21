package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPlayerHandEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link ExileTargetPlayerHandEffect}: every card in the targeted player's hand is exiled.
 * There is no player choice and no play permission; exiling from hand is not a discard, so no discard
 * triggers fire. Mirrors the graveyard-exile log strings.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTargetPlayerHandEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetPlayerHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        String playerName = gameData.playerIdToName.get(targetPlayerId);
        List<Card> hand = gameData.playerHands.get(targetPlayerId);

        if (hand == null || hand.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + "'s hand is already empty."));
            return;
        }

        int count = hand.size();
        List<Card> toExile = new ArrayList<>(hand);
        hand.clear();
        for (Card card : toExile) {
            gameData.addToExile(targetPlayerId, card);
        }

        String logEntry = playerName + "'s hand is exiled (" + count + " card" + (count != 1 ? "s" : "") + ").";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));

        log.info("Game {} - {}'s hand ({} cards) exiled", gameData.id, playerName, count);
    }
}
