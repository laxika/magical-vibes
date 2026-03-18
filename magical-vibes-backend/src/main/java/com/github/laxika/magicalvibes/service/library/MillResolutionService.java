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
import com.github.laxika.magicalvibes.model.effect.MillControllerEffect;
import com.github.laxika.magicalvibes.model.effect.MillBottomOfTargetLibraryConditionalTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MillHalfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerByChargeCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerAndBoostSelfByManaValueEffect;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.CreateCreatureTokenEffect;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.PermanentControlResolutionService;
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
    private final GameQueryService gameQueryService;
    private final PermanentControlResolutionService permanentControlResolutionService;

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
     * Mills the controller for a fixed number of cards (self-mill, no target).
     * Used by cards like Armored Skaab.
     */
    @HandlesEffect(MillControllerEffect.class)
    void resolveMillController(GameData gameData, StackEntry entry, MillControllerEffect effect) {
        graveyardService.resolveMillPlayer(gameData, entry.getControllerId(), effect.count());
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

    /**
     * Puts the bottom card of the target player's library into their graveyard.
     * If the card matches the specified type, the controller creates a creature token.
     * Used by Cellar Door.
     */
    @HandlesEffect(MillBottomOfTargetLibraryConditionalTokenEffect.class)
    void resolveMillBottomOfTargetLibraryConditionalToken(GameData gameData, StackEntry entry,
                                                          MillBottomOfTargetLibraryConditionalTokenEffect effect) {
        UUID targetPlayerId = entry.getTargetPermanentId();
        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        String targetPlayerName = gameData.playerIdToName.get(targetPlayerId);
        String sourceName = entry.getCard().getName();

        if (deck.isEmpty()) {
            String logEntry = targetPlayerName + "'s library is empty — " + sourceName + "'s ability does nothing.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} ability: {}'s library is empty", gameData.id, sourceName, targetPlayerName);
            return;
        }

        Card bottomCard = deck.removeLast();
        graveyardService.addCardToGraveyard(gameData, targetPlayerId, bottomCard);

        String logEntry = targetPlayerName + " puts " + bottomCard.getName() + " from the bottom of their library into their graveyard.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} ability: {} puts {} from bottom of library into graveyard",
                gameData.id, sourceName, targetPlayerName, bottomCard.getName());

        if (bottomCard.hasType(effect.conditionType())) {
            CreateCreatureTokenEffect tokenEffect = new CreateCreatureTokenEffect(
                    effect.tokenName(), effect.tokenPower(), effect.tokenToughness(),
                    effect.tokenColor(), effect.tokenSubtypes(),
                    Set.of(), Set.of()
            );
            permanentControlResolutionService.applyCreateCreatureToken(
                    gameData, entry.getControllerId(), tokenEffect, entry.getCard().getSetCode()
            );
        }
    }

    /**
     * Mills one card from the target player's library, then boosts the source creature
     * by +X/+X until end of turn, where X is the milled card's mana value.
     * Used by Mindshrieker.
     */
    @HandlesEffect(MillTargetPlayerAndBoostSelfByManaValueEffect.class)
    void resolveMillTargetPlayerAndBoostSelfByManaValue(GameData gameData, StackEntry entry) {
        UUID targetPlayerId = entry.getTargetPermanentId();
        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        String cardName = entry.getCard().getName();
        String targetPlayerName = gameData.playerIdToName.get(targetPlayerId);

        if (deck.isEmpty()) {
            String logEntry = targetPlayerName + "'s library is empty — " + cardName + "'s ability does nothing.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        // Snapshot the top card before milling to get its mana value
        Card topCard = deck.getFirst();
        int manaValue = topCard.getManaValue();

        // Mill one card
        graveyardService.resolveMillPlayer(gameData, targetPlayerId, 1);

        // Boost the source creature
        Permanent self = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (self == null) {
            return;
        }

        if (manaValue > 0) {
            self.setPowerModifier(self.getPowerModifier() + manaValue);
            self.setToughnessModifier(self.getToughnessModifier() + manaValue);
        }

        String logEntry = cardName + " gets +" + manaValue + "/+" + manaValue
                + " until end of turn (milled " + topCard.getName() + ", mana value " + manaValue + ").";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} gets +{}/+{} from milling {}", gameData.id, cardName, manaValue, manaValue, topCard.getName());
    }

    private static String pluralCards(int count) {
        return count + " card" + (count != 1 ? "s" : "");
    }
}
