package com.github.laxika.magicalvibes.service.library;

import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.effect.HandlesEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.EachOpponentMillsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsRepeatOnDuplicateEffect;
import com.github.laxika.magicalvibes.model.effect.MillByHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.MillHalfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerByChargeCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerEffect;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Resolves mill-related card effects during stack resolution.
 *
 * <p>Handles effects that move cards from the top of a player's library to their
 * graveyard or exile zone, including fixed-count mills, variable mills (by hand
 * size, charge counters, half library), and exile-based repetition effects.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MillResolutionService {

    private final GraveyardService graveyardService;
    private final GameBroadcastService gameBroadcastService;

    /**
     * Mills the target player for a number of cards equal to their hand size.
     * Used by cards like Dreamborn Muse.
     */
    @HandlesEffect(MillByHandSizeEffect.class)
    void resolveMillByHandSize(GameData gameData, StackEntry entry) {
        UUID targetPlayerId = entry.getTargetPermanentId();
        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        int handSize = hand != null ? hand.size() : 0;

        if (handSize == 0) {
            String playerName = gameData.playerIdToName.get(targetPlayerId);
            String logEntry = playerName + " has no cards in hand — mills nothing.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        graveyardService.resolveMillPlayer(gameData, targetPlayerId, handSize);
    }

    /**
     * Mills the target player for a fixed number of cards specified by the effect.
     */
    @HandlesEffect(MillTargetPlayerEffect.class)
    void resolveMillTargetPlayer(GameData gameData, StackEntry entry, MillTargetPlayerEffect mill) {
        graveyardService.resolveMillPlayer(gameData, entry.getTargetPermanentId(), mill.count());
    }

    /**
     * Mills each opponent for a fixed number of cards.
     */
    @HandlesEffect(EachOpponentMillsEffect.class)
    void resolveEachOpponentMills(GameData gameData, StackEntry entry, EachOpponentMillsEffect effect) {
        UUID controllerId = entry.getControllerId();
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(controllerId)) continue;
            graveyardService.resolveMillPlayer(gameData, playerId, effect.count());
        }
    }

    /**
     * Exiles cards from the top of the target player's library one at a time,
     * repeating until a card with a duplicate name is exiled.
     */
    @HandlesEffect(ExileTopCardsRepeatOnDuplicateEffect.class)
    void resolveExileTopCardsRepeatOnDuplicate(GameData gameData, StackEntry entry, ExileTopCardsRepeatOnDuplicateEffect effect) {
        UUID targetPlayerId = entry.getTargetPermanentId();
        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        List<Card> exiled = gameData.playerExiledCards.get(targetPlayerId);
        String playerName = gameData.playerIdToName.get(targetPlayerId);
        String creatureName = entry.getCard().getName();

        String triggerLog = creatureName + "'s ability triggers — " + playerName + " exiles cards from the top of their library.";
        gameBroadcastService.logAndBroadcast(gameData, triggerLog);

        boolean repeat = true;
        while (repeat) {
            repeat = false;

            if (deck.isEmpty()) {
                String logEntry = playerName + "'s library is empty. No cards to exile.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                break;
            }

            int cardsToExile = Math.min(effect.count(), deck.size());
            List<Card> exiledThisRound = new ArrayList<>();
            for (int i = 0; i < cardsToExile; i++) {
                Card card = deck.removeFirst();
                exiled.add(card);
                exiledThisRound.add(card);
            }

            StringBuilder sb = new StringBuilder();
            sb.append(playerName).append(" exiles ");
            for (int i = 0; i < exiledThisRound.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(exiledThisRound.get(i).getName());
            }
            sb.append(".");
            gameBroadcastService.logAndBroadcast(gameData, sb.toString());

            Set<String> seen = new HashSet<>();
            for (Card card : exiledThisRound) {
                if (!seen.add(card.getName())) {
                    repeat = true;
                    break;
                }
            }

            if (repeat) {
                String repeatLog = "Two or more exiled cards share the same name — repeating the process.";
                gameBroadcastService.logAndBroadcast(gameData, repeatLog);
            }
        }

        log.info("Game {} - {} exile trigger resolved for {}", gameData.id, creatureName, playerName);
    }

    /**
     * Mills the target player for a number of cards equal to the source permanent's charge counters.
     * Used by cards like Grindclock.
     */
    @HandlesEffect(MillTargetPlayerByChargeCountersEffect.class)
    void resolveMillTargetPlayerByChargeCounters(GameData gameData, StackEntry entry) {
        UUID targetPlayerId = entry.getTargetPermanentId();
        int chargeCounters = entry.getXValue();

        if (chargeCounters <= 0) {
            String playerName = gameData.playerIdToName.get(targetPlayerId);
            String logEntry = playerName + " mills 0 cards (no charge counters).";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} mills 0 cards (no charge counters)", gameData.id, playerName);
            return;
        }

        graveyardService.resolveMillPlayer(gameData, targetPlayerId, chargeCounters);
    }

    /**
     * Mills half the target player's library, rounded down.
     * Used by cards like Traumatize.
     */
    @HandlesEffect(MillHalfLibraryEffect.class)
    void resolveMillHalfLibrary(GameData gameData, StackEntry entry) {
        UUID targetPlayerId = entry.getTargetPermanentId();
        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        String playerName = gameData.playerIdToName.get(targetPlayerId);

        int cardsToMill = deck.size() / 2;
        if (cardsToMill == 0) {
            String logEntry = playerName + "'s library has " + pluralCards(deck.size()) + " — mills nothing.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        graveyardService.resolveMillPlayer(gameData, targetPlayerId, cardsToMill);
    }

    private static String pluralCards(int count) {
        return count + " card" + (count != 1 ? "s" : "");
    }
}
