package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Shared logic for Worldpurge's "each player chooses up to N cards in their hand to keep, then
 * shuffles the rest into their library" step. Players choose in APNAP order, one at a time: each
 * chooser is prompted via {@link PendingInteraction.KeepCardsInHandChoice}, and answering advances
 * to the next remaining player with a non-empty hand.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KeepCardsInHandSupport {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final GameBroadcastService gameBroadcastService;

    /**
     * Begins the keep-choice for the first player in {@code orderedPlayerIds} who has a non-empty
     * hand, deferring the rest as {@code remainingPlayerIds}. Returns {@code true} when an
     * interaction was begun (resolution must pause), {@code false} when no player has cards to keep.
     */
    public boolean beginNextChoice(GameData gameData, List<UUID> orderedPlayerIds, int keepCount, String cardName) {
        for (int i = 0; i < orderedPlayerIds.size(); i++) {
            UUID playerId = orderedPlayerIds.get(i);
            List<Card> hand = gameData.playerHands.get(playerId);
            if (hand == null || hand.isEmpty()) {
                continue;
            }
            List<UUID> validCardIds = hand.stream().map(Card::getId).toList();
            List<UUID> remaining = new ArrayList<>(orderedPlayerIds.subList(i + 1, orderedPlayerIds.size()));
            interactionHandlerRegistry.begin(gameData,
                    new PendingInteraction.KeepCardsInHandChoice(playerId, validCardIds, keepCount, remaining, cardName));
            log.info("Game {} - Awaiting {} to choose up to {} cards to keep ({})",
                    gameData.id, gameData.playerIdToName.get(playerId), Math.min(keepCount, validCardIds.size()), cardName);
            return true;
        }
        return false;
    }

    /**
     * Keeps {@code keptCardIds} in {@code playerId}'s hand and shuffles the rest of their hand into
     * their library.
     */
    public void applyKeepChoice(GameData gameData, UUID playerId, List<UUID> keptCardIds, String cardName) {
        List<Card> hand = gameData.playerHands.get(playerId);
        String playerName = gameData.playerIdToName.get(playerId);
        if (hand == null || hand.isEmpty()) {
            return;
        }

        Set<UUID> keptIds = new HashSet<>(keptCardIds);
        List<Card> toShuffle = new ArrayList<>();
        hand.removeIf(card -> {
            if (!keptIds.contains(card.getId())) {
                toShuffle.add(card);
                return true;
            }
            return false;
        });

        if (toShuffle.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData,
                    playerName + " keeps their hand (" + LibraryShuffleSupport.pluralCards(hand.size())
                            + ") — nothing shuffled into their library (" + cardName + ").");
            return;
        }

        List<Card> deck = gameData.playerDecks.get(playerId);
        deck.addAll(toShuffle);
        LibraryShuffleHelper.shuffleLibrary(gameData, playerId);

        gameBroadcastService.logAndBroadcast(gameData,
                playerName + " keeps " + LibraryShuffleSupport.pluralCards(hand.size()) + " and shuffles "
                        + LibraryShuffleSupport.pluralCards(toShuffle.size()) + " into their library (" + cardName + ").");
        log.info("Game {} - {} keeps {} and shuffles {} into library ({})",
                gameData.id, playerName, hand.size(), toShuffle.size(), cardName);
    }
}
