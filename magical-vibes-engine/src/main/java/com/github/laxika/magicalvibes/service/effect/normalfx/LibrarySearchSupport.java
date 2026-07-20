package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.effect.CantSearchLibrariesEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentSearchesTopCardsInsteadEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
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
     * Starts the next pending "each player searches for a basic land" search from the
     * follow-up's remaining-searchers list; the advanced remainder rides the begun search.
     * Returns true if a search was initiated, false if no searcher remains.
     * Respects {@code followUp.eachPlayerSearchTapped()} for the destination.
     */
    public boolean startNextEachPlayerBasicLandSearch(GameData gameData, LibrarySearchFollowUp followUp) {
        LibrarySearchDestination destination = followUp.eachPlayerSearchTapped()
                ? LibrarySearchDestination.BATTLEFIELD_TAPPED
                : LibrarySearchDestination.BATTLEFIELD;
        String prompt = followUp.eachPlayerSearchTapped()
                ? "You may search your library for a basic land card and put it onto the battlefield tapped."
                : "Search your library for a basic land card and put it onto the battlefield.";

        List<UUID> remaining = new ArrayList<>(followUp.remainingEachPlayerBasicLandSearches());
        while (!remaining.isEmpty()) {
            UUID nextPlayerId = remaining.remove(0);
            boolean started = performLibrarySearch(
                    gameData,
                    nextPlayerId,
                    card -> card.hasType(CardType.LAND) && card.getSupertypes().contains(CardSupertype.BASIC),
                    "basic land cards",
                    prompt,
                    false,
                    true,
                    destination,
                    followUp.withRemainingEachPlayerBasicLandSearches(remaining)
            );
            if (started) {
                return true;
            }
            // If search could not start (empty library, Leonin Arbiter, etc.), try the next player
        }
        return false;
    }

    /**
     * Starts the next pending "each player may search for up to N creature cards to hand" search
     * from the follow-up's remaining-searchers list; the advanced remainder rides the begun search.
     * Each searcher may take up to {@code followUp.eachPlayerCreatureToHandCount()} creature cards,
     * revealing them to hand, then shuffles. Returns true if a search was initiated, false if no
     * searcher remains (empty library / no creatures / Leonin Arbiter players are skipped). Used by
     * Weird Harvest.
     */
    public boolean startNextEachPlayerCreatureToHandSearch(GameData gameData, LibrarySearchFollowUp followUp) {
        int count = followUp.eachPlayerCreatureToHandCount();
        List<UUID> remaining = new ArrayList<>(followUp.remainingEachPlayerCreatureToHandSearches());
        while (!remaining.isEmpty()) {
            UUID nextPlayerId = remaining.remove(0);
            String playerName = gameData.playerIdToName.get(nextPlayerId);

            if (isSearchPrevented(gameData, nextPlayerId)) {
                continue;
            }

            List<Card> deck = gameData.playerDecks.get(nextPlayerId);
            if (deck == null || deck.isEmpty()) {
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " searches their library but it is empty. Library is shuffled."));
                continue;
            }

            List<Card> creatures = deck.stream()
                    .filter(card -> card.hasType(CardType.CREATURE))
                    .toList();

            if (creatures.isEmpty()) {
                LibraryShuffleHelper.shuffleLibrary(gameData, nextPlayerId);
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " searches their library but finds no creature cards. Library is shuffled."));
                continue;
            }

            String prompt = "You may search your library for up to " + count + " creature card"
                    + (count == 1 ? "" : "s") + " to reveal and put into your hand.";
            LibrarySearchParams params = LibrarySearchParams.builder(nextPlayerId, new ArrayList<>(creatures))
                    .reveals(true)
                    .canFailToFind(true)
                    .remainingCount(count)
                    .destination(LibrarySearchDestination.HAND)
                    .filterPredicate(new CardTypePredicate(CardType.CREATURE))
                    .followUp(followUp.withRemainingEachPlayerCreatureToHandSearches(remaining))
                    .build();

            interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibrarySearch(params, prompt, true));
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " searches their library."));
            return true;
        }
        return false;
    }

    /**
     * Starts the next pending "each opponent may search for a creature card to battlefield" search
     * from the follow-up's remaining-searchers list; the advanced remainder rides the begun search.
     * Each searcher may search their library for a creature card, put it onto the battlefield, then
     * shuffle. Returns true if a search was initiated, false if no searcher remains (empty library /
     * no creatures / Leonin Arbiter players are skipped). Used by Boldwyr Heavyweights.
     */
    public boolean startNextEachPlayerCreatureToBattlefieldSearch(GameData gameData, LibrarySearchFollowUp followUp) {
        List<UUID> remaining = new ArrayList<>(followUp.remainingEachPlayerCreatureToBattlefieldSearches());
        while (!remaining.isEmpty()) {
            UUID nextPlayerId = remaining.remove(0);
            boolean started = performLibrarySearch(
                    gameData,
                    nextPlayerId,
                    card -> card.hasType(CardType.CREATURE),
                    "creature cards",
                    "You may search your library for a creature card and put it onto the battlefield.",
                    false,
                    true,
                    LibrarySearchDestination.BATTLEFIELD,
                    followUp.withRemainingEachPlayerCreatureToBattlefieldSearches(remaining)
            );
            if (started) {
                return true;
            }
            // If search could not start (empty library, no creatures, Leonin Arbiter, etc.), try the next player
        }
        return false;
    }

    /**
     * Starts the next pending "search for a card with the same name and put it onto the battlefield
     * tapped" pick from the follow-up's same-name queue (Clarion Ultimatum). Each queue entry is one
     * chosen permanent's name; the advanced remainder rides the begun search. Names with no matching
     * card in the library are skipped. Returns true if a search was initiated, false if the queue is
     * exhausted, search is prevented, or the library is empty.
     */
    public boolean startNextSameNamePick(GameData gameData, UUID playerId, LibrarySearchFollowUp followUp) {
        if (isSearchPrevented(gameData, playerId)) return false;

        List<Card> deck = gameData.playerDecks.get(playerId);
        List<String> remaining = new ArrayList<>(followUp.remainingSameNamePicks());
        while (!remaining.isEmpty()) {
            String name = remaining.remove(0);
            if (deck == null || deck.isEmpty()) {
                return false;
            }
            List<Card> matches = deck.stream().filter(card -> name.equals(card.getName())).toList();
            if (matches.isEmpty()) {
                continue;
            }
            String prompt = "You may search your library for a card named " + name
                    + " and put it onto the battlefield tapped.";
            sendLibrarySearchToPlayer(gameData, playerId,
                    LibrarySearchParams.builder(playerId, new ArrayList<>(matches))
                            .canFailToFind(true)
                            .filterCardName(name)
                            .destination(LibrarySearchDestination.BATTLEFIELD_TAPPED)
                            .followUp(followUp.withRemainingSameNamePicks(remaining))
                            .build(), prompt, true);
            return true;
        }
        return false;
    }

    /**
     * Starts the next "search for a card of the queued colour, reveal it, put it into your hand" pick
     * from the follow-up's colour queue (Conflux). Each queue entry is one colour, searched in order;
     * the advanced remainder rides the begun search. Colours with no matching card left in the library
     * are skipped without shuffling. When the queue is exhausted the library is shuffled once (the
     * single shuffle for the whole search) and false is returned; returns true if a search was begun,
     * false if the search is prevented or no colour remains to search.
     */
    public boolean startNextColorToHandPick(GameData gameData, UUID playerId, LibrarySearchFollowUp followUp) {
        if (isSearchPrevented(gameData, playerId)) return false;

        List<Card> deck = gameData.playerDecks.get(playerId);
        String playerName = gameData.playerIdToName.get(playerId);
        List<CardColor> remaining = new ArrayList<>(followUp.remainingColorToHandPicks());
        while (!remaining.isEmpty()) {
            CardColor color = remaining.remove(0);
            List<Card> matches = deck == null ? List.of()
                    : deck.stream().filter(card -> card.getColors().contains(color)).toList();
            if (matches.isEmpty()) {
                continue;
            }
            String colorName = color.name().toLowerCase();
            sendLibrarySearchToPlayer(gameData, playerId,
                    LibrarySearchParams.builder(playerId, new ArrayList<>(matches))
                            .reveals(true)
                            .canFailToFind(true)
                            .destination(LibrarySearchDestination.HAND)
                            .shuffleAfterSelection(false)
                            .followUp(followUp.withRemainingColorToHandPicks(remaining))
                            .build(),
                    "Search your library for a " + colorName + " card to reveal and put into your hand.", true,
                    playerName + " searches their library for a " + colorName + " card.");
            return true;
        }

        // Every colour has been searched; the single shuffle for the whole search happens now.
        if (deck != null) {
            LibraryShuffleHelper.shuffleLibrary(gameData, playerId);
        }
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + "'s library is shuffled."));
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
        return performLibrarySearch(gameData, controllerId, filter, noMatchDescription, prompt,
                reveals, canFailToFind, destination, LibrarySearchFollowUp.NONE);
    }

    public boolean performLibrarySearch(
            GameData gameData,
            UUID controllerId,
            Predicate<Card> filter,
            String noMatchDescription,
            String prompt,
            boolean reveals,
            boolean canFailToFind,
            LibrarySearchDestination destination,
            LibrarySearchFollowUp followUp) {
        if (isSearchPrevented(gameData, controllerId)) return false;

        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);

        if (deck == null || deck.isEmpty()) {
            String logMsg = playerName + " searches their library but it is empty. Library is shuffled.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logMsg));
            return false;
        }

        List<Card> matchingCards = deck.stream().filter(filter).toList();

        if (matchingCards.isEmpty()) {
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            String logMsg = playerName + " searches their library but finds no " + noMatchDescription + ". Library is shuffled.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logMsg));
            log.info("Game {} - {} searches library, no {} found", gameData.id, playerName, noMatchDescription);
            return false;
        }

        sendLibrarySearchToPlayer(gameData, controllerId, LibrarySearchParams.builder(controllerId, matchingCards)
                .reveals(reveals)
                .canFailToFind(canFailToFind)
                .prompt(prompt)
                .destination(destination)
                .followUp(followUp)
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
                    if (effect instanceof CantSearchLibrariesEffect restriction) {
                        boolean paid = false;
                        if (restriction.payableToIgnore()) {
                            Set<UUID> paidSet = gameData.paidSearchTaxPermanentIds.get(searchingPlayerId);
                            paid = paidSet != null && paidSet.contains(perm.getId());
                        }
                        if (!paid) {
                            String playerName = gameData.playerIdToName.get(searchingPlayerId);
                            String sourceName = perm.getCard().getName();
                            String logMsg = playerName + "'s library search is prevented by " + sourceName + ".";
                            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logMsg));
                            log.info("Game {} - {} search prevented by {}",
                                    gameData.id, playerName, sourceName);
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
        // Aven Mindcensor & friends: an opponent's search is limited to the top N cards of that library.
        int topLimit = opponentSearchTopCardsLimit(gameData, params.playerId());
        if (topLimit != Integer.MAX_VALUE) {
            UUID libraryOwnerId = params.targetPlayerId() != null ? params.targetPlayerId() : params.playerId();
            List<Card> restricted = restrictToTopCards(gameData, libraryOwnerId, params.cards(), topLimit);
            if (restricted.isEmpty()) {
                // None of the top N cards match the search: the player searched but found nothing.
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logMessage));
                if (params.shuffleAfterSelection()) {
                    LibraryShuffleHelper.shuffleLibrary(gameData, libraryOwnerId);
                }
                String searcherName = gameData.playerIdToName.get(params.playerId());
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(
                        searcherName + " finds no matching card among the top " + topLimit
                                + " cards. Library is shuffled."));
                return;
            }
            params = params.withCards(restricted);
        }

        interactionHandlerRegistry.begin(gameData, new com.github.laxika.magicalvibes.model.PendingInteraction.LibrarySearch(
                params, prompt, canFailToFind));

        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logMessage));
    }

    /**
     * The strictest "search only the top N cards" limit imposed on {@code searchingPlayerId} by an
     * {@link OpponentSearchesTopCardsInsteadEffect} (Aven Mindcensor) that one of their opponents
     * controls, or {@link Integer#MAX_VALUE} when none applies. A player's own copy never limits
     * their own searches — the effect only cares about opponents.
     */
    public int opponentSearchTopCardsLimit(GameData gameData, UUID searchingPlayerId) {
        int limit = Integer.MAX_VALUE;
        for (UUID pid : gameData.orderedPlayerIds) {
            if (pid.equals(searchingPlayerId)) continue;
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof OpponentSearchesTopCardsInsteadEffect restriction) {
                        limit = Math.min(limit, restriction.count());
                    }
                }
            }
        }
        return limit;
    }

    /**
     * Narrows {@code candidates} to only the cards that are among the top {@code limit} cards of
     * {@code libraryOwnerId}'s library, preserving candidate order. Uses reference identity so a
     * duplicate-named card deeper in the library is not wrongly treated as searchable.
     */
    private List<Card> restrictToTopCards(GameData gameData, UUID libraryOwnerId, List<Card> candidates, int limit) {
        List<Card> deck = gameData.playerDecks.get(libraryOwnerId);
        if (deck == null || deck.isEmpty()) return candidates;
        List<Card> top = deck.subList(0, Math.min(limit, deck.size()));
        Set<Card> topCards = Collections.newSetFromMap(new IdentityHashMap<>());
        topCards.addAll(top);
        return candidates.stream().filter(topCards::contains).toList();
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
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logMsg));
    }

    public static String pluralCards(int count) {
        return count + " card" + (count != 1 ? "s" : "");
    }
}
