package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CantSearchLibrariesEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.ChooseCardFromLibraryMessage;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Shared library search helpers used by every normal Library Search effect handler and by
 * input handlers that continue async search flows.
 *
 * <p>Extracted verbatim from the original {@code LibrarySearchResolutionService} monolith;
 * behavior (log strings, interaction ordering, shuffle timing) is identical.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LibrarySearchSupport {

    private final GameBroadcastService gameBroadcastService;
    private final com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry interactionHandlerRegistry;

    /**
     * Starts the next pending "each player searches for a basic land" search from the queue.
     * Returns true if a search was initiated, false if the queue is empty.
     * Respects {@code pendingEachPlayerBasicLandSearchTapped} for the destination.
     */
    public boolean startNextEachPlayerBasicLandSearch(GameData gameData) {
        LibrarySearchDestination destination = gameData.pendingEachPlayerBasicLandSearchTapped
                ? LibrarySearchDestination.BATTLEFIELD_TAPPED
                : LibrarySearchDestination.BATTLEFIELD;
        String prompt = gameData.pendingEachPlayerBasicLandSearchTapped
                ? "You may search your library for a basic land card and put it onto the battlefield tapped."
                : "Search your library for a basic land card and put it onto the battlefield.";

        while (!gameData.pendingEachPlayerBasicLandSearchQueue.isEmpty()) {
            UUID nextPlayerId = gameData.pendingEachPlayerBasicLandSearchQueue.pollFirst();
            boolean started = performLibrarySearch(
                    gameData,
                    nextPlayerId,
                    card -> card.hasType(CardType.LAND) && card.getSupertypes().contains(CardSupertype.BASIC),
                    "basic land cards",
                    prompt,
                    false,
                    true,
                    destination
            );
            if (started) {
                return true;
            }
            // If search could not start (empty library, Leonin Arbiter, etc.), try the next player
        }
        return false;
    }

    /**
     * Searches the controller's library for a creature card, reveals it, and puts it into their hand.
     * Called after the sacrifice portion of SacrificeCreatureSearchLibraryForCreatureToHandEffect completes.
     */
    public void searchLibraryForCreatureToHand(GameData gameData, UUID controllerId) {
        performLibrarySearch(
                gameData,
                controllerId,
                card -> card.hasType(CardType.CREATURE),
                "creature cards",
                "Search your library for a creature card to reveal and put into your hand.",
                true,
                true,
                LibrarySearchDestination.HAND
        );
    }

    /**
     * Unified library search skeleton: check restriction -> get deck -> filter -> handle no matches ->
     * begin interaction -> send message -> log. Returns true if the search was initiated, false otherwise.
     */
    public boolean performLibrarySearch(
            GameData gameData,
            UUID controllerId,
            Predicate<Card> filter,
            String noMatchDescription,
            String prompt,
            boolean reveals,
            boolean canFailToFind,
            LibrarySearchDestination destination) {
        if (isSearchPrevented(gameData, controllerId)) return false;

        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);

        if (deck == null || deck.isEmpty()) {
            String logMsg = playerName + " searches their library but it is empty. Library is shuffled.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            return false;
        }

        List<Card> matchingCards = deck.stream().filter(filter).toList();

        if (matchingCards.isEmpty()) {
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            String logMsg = playerName + " searches their library but finds no " + noMatchDescription + ". Library is shuffled.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            log.info("Game {} - {} searches library, no {} found", gameData.id, playerName, noMatchDescription);
            return false;
        }

        sendLibrarySearchToPlayer(gameData, controllerId, LibrarySearchParams.builder(controllerId, matchingCards)
                .reveals(reveals)
                .canFailToFind(canFailToFind)
                .prompt(prompt)
                .destination(destination)
                .build(), prompt, canFailToFind);

        log.info("Game {} - {} searches their library ({} matches)", gameData.id, playerName, matchingCards.size());
        return true;
    }

    /**
     * Checks if a library search is prevented by Leonin Arbiter (CantSearchLibrariesEffect).
     * Returns true if the search may proceed (no unpaid Arbiters), false if prevented.
     * Payment is handled as a special action during priority (before the spell resolves).
     */
    public boolean checkSearchRestriction(GameData gameData, UUID searchingPlayerId) {
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof CantSearchLibrariesEffect) {
                        Set<UUID> paidSet = gameData.paidSearchTaxPermanentIds.get(searchingPlayerId);
                        if (paidSet == null || !paidSet.contains(perm.getId())) {
                            String playerName = gameData.playerIdToName.get(searchingPlayerId);
                            String logMsg = playerName + "'s library search is prevented by Leonin Arbiter.";
                            gameBroadcastService.logAndBroadcast(gameData, logMsg);
                            log.info("Game {} - {} has unpaid Leonin Arbiter search tax, search prevented",
                                    gameData.id, playerName);
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    public String formatCardTypeSetForPrompt(Set<CardType> cardTypes) {
        if (cardTypes == null || cardTypes.isEmpty()) {
            return "matching";
        }
        List<String> names = cardTypes.stream()
                .map(type -> type.name().toLowerCase())
                .sorted()
                .toList();
        if (names.size() == 1) {
            return names.getFirst();
        }
        return String.join(" or ", names);
    }

    public static boolean matchesCardTypes(Card card, Set<CardType> cardTypes) {
        return cardTypes.contains(card.getType())
                || card.getAdditionalTypes().stream().anyMatch(cardTypes::contains);
    }

    public void sendLibrarySearchToPlayer(GameData gameData, UUID playerId, LibrarySearchParams params,
                                            String prompt, boolean canFailToFind) {
        String playerName = gameData.playerIdToName.get(playerId);
        sendLibrarySearchToPlayer(gameData, playerId, params, prompt, canFailToFind,
                playerName + " searches their library.");
    }

    public void sendLibrarySearchToPlayer(GameData gameData, UUID playerId, LibrarySearchParams params,
                                            String prompt, boolean canFailToFind, String logMessage) {
        interactionHandlerRegistry.begin(gameData, new com.github.laxika.magicalvibes.model.PendingInteraction.LibrarySearch(
                params, prompt, canFailToFind));

        gameBroadcastService.logAndBroadcast(gameData, logMessage);
    }

    public boolean isSearchPrevented(GameData gameData, UUID searchingPlayerId) {
        if (checkSearchRestriction(gameData, searchingPlayerId)) return false;
        List<Card> deck = gameData.playerDecks.get(searchingPlayerId);
        if (deck != null) LibraryShuffleHelper.shuffleLibrary(gameData, searchingPlayerId);
        return true;
    }

    public void putHandOnTopOfLibrary(GameData gameData, List<Card> hand, List<Card> deck, String playerName) {
        int handSize = hand.size();
        if (handSize == 0) return;
        for (int i = handSize - 1; i >= 0; i--) {
            deck.addFirst(hand.get(i));
        }
        hand.clear();
        String logMsg = playerName + " puts " + pluralCards(handSize)
                + " from their hand on top of their library.";
        gameBroadcastService.logAndBroadcast(gameData, logMsg);
    }

    public static String pluralCards(int count) {
        return count + " card" + (count != 1 ? "s" : "");
    }
}
