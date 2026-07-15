package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutHandOnBottomOfLibraryAndDrawEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PutHandOnBottomOfLibraryAndDrawEffectHandler implements NormalEffectHandlerBean {

    private final DrawService drawService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutHandOnBottomOfLibraryAndDrawEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID playerId = entry.getTargetId();
        List<Card> hand = gameData.playerHands.get(playerId);
        String playerName = gameData.playerIdToName.get(playerId);
        String sourceName = entry.getCard().getName();

        if (hand == null || hand.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " has no cards in hand (" + sourceName + ")."));
            log.info("Game {} - {} has no cards in hand for {}", gameData.id, playerName, sourceName);
            return;
        }

        int handSize = hand.size();

        // Put the entire hand on the bottom of the library (order is hidden, so no reorder prompt).
        List<Card> deck = gameData.playerDecks.get(playerId);
        deck.addAll(hand);
        hand.clear();

        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " puts " + handSize + " card" + (handSize != 1 ? "s" : "")
                        + " from hand on the bottom of their library (" + sourceName + ")."));
        log.info("Game {} - {} puts {} cards from hand on bottom of library ({})",
                gameData.id, playerName, handSize, sourceName);

        // Draw that many cards.
        for (int i = 0; i < handSize; i++) {
            drawService.resolveDrawCard(gameData, playerId);
        }

        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " draws " + handSize + " card" + (handSize != 1 ? "s" : "") + "."));
        log.info("Game {} - {} draws {} cards ({})", gameData.id, playerName, handSize, sourceName);
    }
}
