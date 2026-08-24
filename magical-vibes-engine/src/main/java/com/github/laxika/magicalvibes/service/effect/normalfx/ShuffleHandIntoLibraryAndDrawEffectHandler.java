package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleHandIntoLibraryAndDrawEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameLogService;
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
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ShuffleHandIntoLibraryAndDrawEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {

        ShuffleHandIntoLibraryAndDrawEffect wheelEffect = (ShuffleHandIntoLibraryAndDrawEffect) effect;
        String cardName = entry.getCard().getName();
        List<UUID> playerIds = wheelEffect.eachPlayer()
                ? gameData.orderedPlayerIds
                : List.of(entry.getControllerId());

        for (UUID playerId : playerIds) {
            List<Card> hand = gameData.playerHands.get(playerId);
            String playerName = gameData.playerIdToName.get(playerId);

            if (hand == null || hand.isEmpty()) {
                gameLogService.append(gameData, GameLog.text(playerName + " has no cards in hand to shuffle."));
                log.info("Game {} - {} has no cards in hand for {}", gameData.id, playerName, cardName);
                continue;
            }

            int handSize = hand.size();

            // Shuffle hand into library
            List<Card> deck = gameData.playerDecks.get(playerId);
            deck.addAll(hand);
            hand.clear();
            LibraryShuffleHelper.shuffleLibrary(gameData, playerId);

            gameLogService.append(gameData, GameLog.text(playerName + " shuffles " + handSize + " card" + (handSize != 1 ? "s" : "")
                            + " from hand into their library."));
            log.info("Game {} - {} shuffles {} cards from hand into library ({})",
                    gameData.id, playerName, handSize, cardName);

            // Draw that many cards
            for (int i = 0; i < handSize; i++) {
                drawService.resolveDrawCard(gameData, playerId);
            }

            gameLogService.append(gameData, GameLog.text(playerName + " draws " + handSize + " card" + (handSize != 1 ? "s" : "") + "."));
            log.info("Game {} - {} draws {} cards ({})", gameData.id, playerName, handSize, cardName);
        }
    
    }
}
