package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleHandIntoLibraryAndDrawEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShuffleHandIntoLibraryAndDrawEffectHandler implements NormalEffectHandlerBean {

    private final DrawService drawService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ShuffleHandIntoLibraryAndDrawEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {

        String cardName = entry.getCard().getName();

        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> hand = gameData.playerHands.get(playerId);
            String playerName = gameData.playerIdToName.get(playerId);

            if (hand == null || hand.isEmpty()) {
                gameBroadcastService.logAndBroadcast(gameData,
                        playerName + " has no cards in hand to shuffle.");
                log.info("Game {} - {} has no cards in hand for {}", gameData.id, playerName, cardName);
                continue;
            }

            int handSize = hand.size();

            // Shuffle hand into library
            List<Card> deck = gameData.playerDecks.get(playerId);
            deck.addAll(hand);
            hand.clear();
            LibraryShuffleHelper.shuffleLibrary(gameData, playerId);

            gameBroadcastService.logAndBroadcast(gameData,
                    playerName + " shuffles " + handSize + " card" + (handSize != 1 ? "s" : "")
                            + " from hand into their library.");
            log.info("Game {} - {} shuffles {} cards from hand into library ({})",
                    gameData.id, playerName, handSize, cardName);

            // Draw that many cards
            for (int i = 0; i < handSize; i++) {
                drawService.resolveDrawCard(gameData, playerId);
            }

            gameBroadcastService.logAndBroadcast(gameData,
                    playerName + " draws " + handSize + " card" + (handSize != 1 ? "s" : "") + ".");
            log.info("Game {} - {} draws {} cards ({})", gameData.id, playerName, handSize, cardName);
        }
    
    }
}
