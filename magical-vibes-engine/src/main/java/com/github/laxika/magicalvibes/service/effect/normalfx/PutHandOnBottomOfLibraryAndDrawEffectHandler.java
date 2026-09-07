package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutHandOnBottomOfLibraryAndDrawEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
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
    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

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
            gameLogService.append(gameData, GameLog.text(playerName + " has no cards in hand (" + sourceName + ")."));
            log.info("Game {} - {} has no cards in hand for {}", gameData.id, playerName, sourceName);
            return;
        }

        int handSize = hand.size();

        List<Card> cardsToBottom = List.copyOf(hand);
        hand.clear();

        if (cardsToBottom.size() > 1) {
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryReorder(
                    playerId, cardsToBottom, true, playerId,
                    "Choose the order of the cards to put on the bottom of your library.", handSize));
            return;
        }

        List<Card> deck = gameData.playerDecks.get(playerId);
        deck.addAll(cardsToBottom);

        gameLogService.append(gameData, GameLog.text(playerName + " puts " + handSize + " card" + (handSize != 1 ? "s" : "")
                        + " from hand on the bottom of their library (" + sourceName + ")."));
        log.info("Game {} - {} puts {} cards from hand on bottom of library ({})",
                gameData.id, playerName, handSize, sourceName);

        // Draw that many cards.
        for (int i = 0; i < handSize; i++) {
            drawService.resolveDrawCard(gameData, playerId);
        }

        gameLogService.append(gameData, GameLog.text(playerName + " draws " + handSize + " card" + (handSize != 1 ? "s" : "") + "."));
        log.info("Game {} - {} draws {} cards ({})", gameData.id, playerName, handSize, sourceName);
    }
}
