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
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp.SameNamePickQueue;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.PendingEachPlayerLibraryExile;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CantSearchLibrariesEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LibrarySearchCastPermission;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantSearchLibrariesEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentSearchesTopCardsInsteadEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.library.LibrarySearchTriggerHelper;
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

    private final GameLogService gameLogService;
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
     * Starts the next pending "each player may search for up to N cards to hand" search from the
     * follow-up's remaining-searchers list; the advanced remainder rides the begun search. Each
     * searcher may take up to {@code followUp.eachPlayerToHandCount()} cards to hand, then shuffles;
     * when {@code followUp.eachPlayerToHandCreatureOnly()} the search is restricted to creature cards
     * and the taken cards are revealed. Returns true if a search was initiated, false if no searcher
     * remains (empty library / no matching cards / Leonin Arbiter players are skipped). Used by
     * Weird Harvest (creature-only) and Noble Benefactor (any card).
     */
    public boolean startNextEachPlayerToHandSearch(GameData gameData, LibrarySearchFollowUp followUp) {
        int count = followUp.eachPlayerToHandCount();
        boolean creatureOnly = followUp.eachPlayerToHandCreatureOnly();
        List<UUID> remaining = new ArrayList<>(followUp.remainingEachPlayerToHandSearches());
        while (!remaining.isEmpty()) {
            UUID nextPlayerId = remaining.remove(0);
            String playerName = gameData.playerIdToName.get(nextPlayerId);

            if (isSearchPrevented(gameData, nextPlayerId)) {
                continue;
            }

            gameData.playersWhoSearchedLibraryThisTurn.add(nextPlayerId);

            List<Card> deck = gameData.playerDecks.get(nextPlayerId);
            if (deck == null || deck.isEmpty()) {
                gameLogService.append(gameData, GameLog.text(playerName + " searches their library but it is empty. Library is shuffled."));
                continue;
            }

            List<Card> choices = creatureOnly
                    ? deck.stream().filter(card -> card.hasType(CardType.CREATURE)).toList()
                    : deck;

            if (choices.isEmpty()) {
                LibraryShuffleHelper.shuffleLibrary(gameData, nextPlayerId);
                gameLogService.append(gameData, GameLog.text(playerName + " searches their library but finds no creature cards. Library is shuffled."));
                continue;
            }

            String prompt = creatureOnly
                    ? "You may search your library for up to " + count + " creature card"
                            + (count == 1 ? "" : "s") + " to reveal and put into your hand."
                    : "You may search your library for up to " + count + " card"
                            + (count == 1 ? "" : "s") + " to put into your hand.";
            LibrarySearchParams params = LibrarySearchParams.builder(nextPlayerId, new ArrayList<>(choices))
                    .reveals(creatureOnly)
                    .canFailToFind(true)
                    .remainingCount(count)
                    .destination(LibrarySearchDestination.HAND)
                    .filterPredicate(creatureOnly ? new CardTypePredicate(CardType.CREATURE) : null)
                    .followUp(followUp.withRemainingEachPlayerToHandSearches(remaining))
                    .build();

            interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibrarySearch(params, prompt, true));
            gameLogService.append(gameData, GameLog.text(playerName + " searches their library."));
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
     * Starts the next pending "each opponent may search for a land card to battlefield" search
     * from the follow-up's remaining-searchers list. Each searcher may take one land card, which
     * enters untapped, then shuffles.
     */
    public boolean startNextEachPlayerLandToBattlefieldSearch(GameData gameData,
                                                               LibrarySearchFollowUp followUp) {
        List<UUID> remaining = new ArrayList<>(followUp.remainingEachPlayerLandToBattlefieldSearches());
        while (!remaining.isEmpty()) {
            UUID nextPlayerId = remaining.remove(0);
            boolean started = performLibrarySearch(
                    gameData,
                    nextPlayerId,
                    card -> card.hasType(CardType.LAND),
                    "land cards",
                    "You may search your library for a land card and put it onto the battlefield.",
                    false,
                    true,
                    LibrarySearchDestination.BATTLEFIELD,
                    followUp.withRemainingEachPlayerLandToBattlefieldSearches(remaining)
            );
            if (started) {
                return true;
            }
        }
        return false;
    }

    /**
     * Starts the next targeted player's mandatory unrestricted search for a card to put on top of
     * their library. Players whose searches cannot start are skipped, while the remaining players
     * continue through the shared library-search interaction flow.
     */
    public boolean startNextTargetPlayerTopSearch(GameData gameData, LibrarySearchFollowUp followUp) {
        List<UUID> remaining = new ArrayList<>(followUp.remainingTargetPlayerTopSearches());
        while (!remaining.isEmpty()) {
            UUID playerId = remaining.remove(0);
            boolean started = performLibrarySearch(
                    gameData,
                    playerId,
                    card -> true,
                    "cards",
                    "Search your library for a card, then shuffle and put that card on top.",
                    false,
                    false,
                    LibrarySearchDestination.TOP_OF_LIBRARY,
                    followUp.withRemainingTargetPlayerTopSearches(remaining));
            if (started) {
                return true;
            }
        }
        return false;
    }

    /**
     * Starts the next pending "search for a card with the same name and put it onto the battlefield"
     * pick from the follow-up's same-name queue (Clarion Ultimatum, Doubling Chant). Each queue entry
     * is one permanent's name; the queue's own destination and creature-only restriction apply to
     * every pick, and the advanced remainder rides the begun search. Names with no matching card in
     * the library are skipped. Returns true if a search was initiated, false if the queue is
     * exhausted or absent, search is prevented, or the library is empty.
     */
    public boolean startNextSameNamePick(GameData gameData, UUID playerId, LibrarySearchFollowUp followUp) {
        if (isSearchPrevented(gameData, playerId)) return false;

        SameNamePickQueue queue = followUp.remainingSameNamePicks();
        if (queue == null) return false;

        boolean tapped = queue.destination() == LibrarySearchDestination.BATTLEFIELD_TAPPED;
        List<Card> deck = gameData.playerDecks.get(playerId);
        List<String> remaining = new ArrayList<>(queue.names());
        while (!remaining.isEmpty()) {
            String name = remaining.remove(0);
            if (deck == null || deck.isEmpty()) {
                return false;
            }
            List<Card> matches = deck.stream()
                    .filter(card -> name.equals(card.getName()))
                    .filter(card -> !queue.creatureOnly() || card.hasType(CardType.CREATURE))
                    .toList();
            if (matches.isEmpty()) {
                continue;
            }
            String prompt = "You may search your library for a " + (queue.creatureOnly() ? "creature card" : "card")
                    + " named " + name + " and put it onto the battlefield" + (tapped ? " tapped." : ".");
            sendLibrarySearchToPlayer(gameData, playerId,
                    LibrarySearchParams.builder(playerId, new ArrayList<>(matches))
                            .canFailToFind(true)
                            .filterCardName(name)
                            .destination(queue.destination())
                            .followUp(followUp.withRemainingSameNamePicks(queue.withNames(remaining)))
                            .build(), prompt, true);
            return true;
        }
        return false;
    }

    /**
     * Advances the queued {@link PendingEachPlayerLibraryExile}: begins the controller's search of
     * the next player's library for a nonland card to exile, skipping players whose library holds
     * no nonland card (they still shuffle) and players nobody may search (Leonin Arbiter). Once
     * every library has been searched the accumulated exiled cards are offered for free casting.
     *
     * <p>Returns {@code true} when a search or the free-cast choice was begun, {@code false} when
     * nothing remains to do and the caller should finish the resolution itself.
     */
    public boolean advanceEachPlayerNonlandExile(GameData gameData) {
        PendingEachPlayerLibraryExile state = gameData.pollPendingInteraction(PendingEachPlayerLibraryExile.class);
        if (state == null) {
            return false;
        }

        UUID searcherId = state.searcherId();
        String searcherName = gameData.playerIdToName.get(searcherId);
        List<UUID> remaining = new ArrayList<>(state.remainingPlayerIds());

        while (!remaining.isEmpty()) {
            UUID targetPlayerId = remaining.removeFirst();
            String targetName = gameData.playerIdToName.get(targetPlayerId);

            if (isSearchPrevented(gameData, searcherId, targetPlayerId, true, searcherId)) {
                LibraryShuffleHelper.shuffleLibrary(gameData, targetPlayerId);
                gameLogService.append(gameData, GameLog.text(targetName + "'s library is shuffled."));
                continue;
            }

            List<Card> deck = gameData.playerDecks.get(targetPlayerId);
            List<Card> nonland = deck == null ? List.of()
                    : deck.stream().filter(card -> !card.hasType(CardType.LAND)).toList();
            if (nonland.isEmpty()) {
                LibraryShuffleHelper.shuffleLibrary(gameData, targetPlayerId);
                gameLogService.append(gameData, GameLog.text(searcherName + " finds no nonland card in "
                        + targetName + "'s library. Library is shuffled."));
                continue;
            }

            gameData.queueInteraction(new PendingEachPlayerLibraryExile(searcherId, remaining, state.exiledCardIds()));
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibrarySearch(
                    LibrarySearchParams.builder(searcherId, new ArrayList<>(nonland))
                            .targetPlayerId(targetPlayerId)
                            .destination(LibrarySearchDestination.EXILE_FOR_FREE_CAST)
                            .shuffleAfterSelection(true)
                            .build(),
                    "Search " + targetName + "'s library for a nonland card to exile.", false));
            gameLogService.append(gameData, GameLog.text(searcherName + " searches " + targetName + "'s library."));
            return true;
        }

        List<UUID> castable = state.exiledCardIds().stream()
                .filter(cardId -> {
                    var exiled = gameData.findExiledCard(cardId);
                    return exiled != null && !exiled.card().hasType(CardType.LAND);
                })
                .toList();
        if (castable.isEmpty()) {
            return false;
        }
        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.ImprovisationCapstoneCastChoice(searcherId, castable, castable.size()));
        return true;
    }

    /**
     * Starts the next "search for a card matching the queued descriptor, reveal it, put it into your
     * hand" pick from the follow-up's queue (a colour for Conflux, a subtype for Gem of Becoming, or
     * an exact card name for Nissa's Encouragement). Each queue entry is one descriptor, searched in
     * order; the advanced remainder rides the begun search. Descriptors with no matching card left
     * in the library are skipped without shuffling. When the queue is exhausted the library is
     * shuffled once (the single shuffle for the whole search) and false is returned; returns true if
     * a search was begun, false if the search is prevented or no descriptor remains to search.
     */
    public boolean startNextToHandPick(GameData gameData, UUID playerId, LibrarySearchFollowUp followUp) {
        if (isSearchPrevented(gameData, playerId)) return false;

        List<Card> deck = gameData.playerDecks.get(playerId);
        String playerName = gameData.playerIdToName.get(playerId);
        List<LibrarySearchFollowUp.ToHandPick> remaining = new ArrayList<>(followUp.remainingToHandPicks());
        while (!remaining.isEmpty()) {
            LibrarySearchFollowUp.ToHandPick pick = remaining.remove(0);
            List<Card> matches = deck == null ? List.of()
                    : deck.stream().filter(card -> matchesToHandPick(card, pick)).toList();
            if (matches.isEmpty()) {
                continue;
            }
            String prompt;
            String logMsg;
            var builder = LibrarySearchParams.builder(playerId, new ArrayList<>(matches))
                    .reveals(true)
                    .canFailToFind(true)
                    .destination(LibrarySearchDestination.HAND)
                    .shuffleAfterSelection(false)
                    .followUp(followUp.withRemainingToHandPicks(remaining));
            if (pick.cardName() != null) {
                builder.filterCardName(pick.cardName());
                prompt = "Search your library for a card named " + pick.cardName()
                        + " to reveal and put into your hand.";
                logMsg = playerName + " searches their library for a card named " + pick.cardName() + ".";
            } else {
                String descriptor = pick.describe();
                prompt = "Search your library for a " + descriptor + " card to reveal and put into your hand.";
                logMsg = playerName + " searches their library for a " + descriptor + " card.";
            }
            sendLibrarySearchToPlayer(gameData, playerId, builder.build(), prompt, true, logMsg);
            return true;
        }

        // Every descriptor has been searched; the single shuffle for the whole search happens now.
        if (deck != null) {
            LibraryShuffleHelper.shuffleLibrary(gameData, playerId);
        }
        gameLogService.append(gameData, GameLog.text(playerName + "'s library is shuffled."));
        return false;
    }

    private static boolean matchesToHandPick(Card card, LibrarySearchFollowUp.ToHandPick pick) {
        if (pick.cardName() != null) {
            return pick.cardName().equals(card.getName());
        }
        return pick.color() != null
                ? card.getColors().contains(pick.color())
                : card.getSubtypes().contains(pick.subtype());
    }

    /**
     * Starts the next "search for an instant card with the queued mana value, reveal it, put it into
     * your hand" pick from the follow-up's mana-value queue (Firemind's Foresight). Each queue entry
     * is one exact mana value, searched in order; values with no matching instant left in the library
     * are skipped without shuffling. When the queue is exhausted the library is shuffled once and
     * false is returned; returns true if a search was begun.
     */
    public boolean startNextInstantManaValueToHandPick(GameData gameData, UUID playerId,
                                                       LibrarySearchFollowUp followUp) {
        if (isSearchPrevented(gameData, playerId)) return false;

        List<Integer> queue = followUp.remainingInstantManaValueToHandPicks();
        if (queue == null) return false;

        List<Card> deck = gameData.playerDecks.get(playerId);
        String playerName = gameData.playerIdToName.get(playerId);
        List<Integer> remaining = new ArrayList<>(queue);
        while (!remaining.isEmpty()) {
            int manaValue = remaining.remove(0);
            List<Card> matches = deck == null ? List.of()
                    : deck.stream()
                            .filter(card -> card.hasType(CardType.INSTANT))
                            .filter(card -> card.getManaValue() == manaValue)
                            .toList();
            if (matches.isEmpty()) {
                continue;
            }
            sendLibrarySearchToPlayer(gameData, playerId,
                    LibrarySearchParams.builder(playerId, new ArrayList<>(matches))
                            .reveals(true)
                            .canFailToFind(true)
                            .destination(LibrarySearchDestination.HAND)
                            .shuffleAfterSelection(false)
                            .followUp(followUp.withRemainingInstantManaValueToHandPicks(remaining))
                            .build(),
                    "Search your library for an instant card with mana value " + manaValue
                            + " to reveal and put into your hand.", true,
                    playerName + " searches their library for an instant card with mana value "
                            + manaValue + ".");
            return true;
        }

        if (deck != null) {
            LibraryShuffleHelper.shuffleLibrary(gameData, playerId);
        }
        gameLogService.append(gameData, GameLog.text(playerName + "'s library is shuffled."));
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
        return performLibrarySearch(gameData, controllerId, filter, noMatchDescription, prompt,
                reveals, canFailToFind, destination, followUp, null);
    }

    /**
     * @param attachToPermanentId when non-null (paired with
     *        {@link LibrarySearchDestination#BATTLEFIELD_ATTACHED_TO_PERMANENT}), the found card
     *        enters the battlefield attached to that permanent.
     */
    public boolean performLibrarySearch(
            GameData gameData,
            UUID controllerId,
            Predicate<Card> filter,
            String noMatchDescription,
            String prompt,
            boolean reveals,
            boolean canFailToFind,
            LibrarySearchDestination destination,
            LibrarySearchFollowUp followUp,
            UUID attachToPermanentId) {
        if (isSearchPrevented(gameData, controllerId)) return false;

        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);

        if (deck == null || deck.isEmpty()) {
            // Searching an empty library is still a search, so opponent-search triggers fire here too
            // (the interaction-starting path fires them in sendLibrarySearchToPlayer).
            LibrarySearchTriggerHelper.checkOpponentSearchTriggers(gameData, gameLogService, controllerId);
            String logMsg = playerName + " searches their library but it is empty. Library is shuffled.";
            gameLogService.append(gameData, GameLog.text(logMsg));
            return false;
        }

        List<Card> matchingCards = deck.stream().filter(filter).toList();

        if (matchingCards.isEmpty()) {
            LibrarySearchTriggerHelper.checkOpponentSearchTriggers(gameData, gameLogService, controllerId);
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            String logMsg = playerName + " searches their library but finds no " + noMatchDescription + ". Library is shuffled.";
            gameLogService.append(gameData, GameLog.text(logMsg));
            log.info("Game {} - {} searches library, no {} found", gameData.id, playerName, noMatchDescription);
            return false;
        }

        sendLibrarySearchToPlayer(gameData, controllerId, LibrarySearchParams.builder(controllerId, matchingCards)
                .reveals(reveals)
                .canFailToFind(canFailToFind)
                .prompt(prompt)
                .destination(destination)
                .followUp(followUp)
                .attachToPermanentId(attachToPermanentId)
                .build(), prompt, canFailToFind);

        log.info("Game {} - {} searches their library ({} matches)", gameData.id, playerName, matchingCards.size());
        return true;
    }

    /**
     * Checks whether a library search is prevented by a static restriction. Payment for a
     * pay-to-ignore restriction is made by {@code searchingPlayerId}; the library owner is
     * separate because some effects make one player search another player's library.
     */
    public boolean checkSearchRestriction(GameData gameData, UUID searchingPlayerId) {
        UUID causingControllerId = resolvingControllerId(gameData);
        return checkSearchRestriction(gameData, searchingPlayerId, searchingPlayerId, causingControllerId);
    }

    public boolean checkSearchRestriction(GameData gameData, UUID searchingPlayerId,
                                          UUID causingControllerId) {
        return checkSearchRestriction(gameData, searchingPlayerId, searchingPlayerId, causingControllerId);
    }

    public boolean checkSearchRestriction(GameData gameData, UUID searchingPlayerId,
                                          UUID libraryOwnerId, UUID causingControllerId) {
        if (gameData.playersCantSearchLibrariesThisTurn) {
            String playerName = gameData.playerIdToName.get(searchingPlayerId);
            gameLogService.append(gameData, GameLog.text(
                    playerName + "'s library search is prevented this turn."));
            log.info("Game {} - {} search prevented this turn", gameData.id, playerName);
            return false;
        }

        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof OpponentsCantSearchLibrariesEffect) {
                        if (causingControllerId == null
                                || !libraryOwnerId.equals(causingControllerId)
                                || pid.equals(causingControllerId)) {
                            continue;
                        }
                        String playerName = gameData.playerIdToName.get(searchingPlayerId);
                        String sourceName = perm.getCard().getName();
                        gameLogService.append(gameData, GameLog.text(
                                playerName + "'s library search is prevented by " + sourceName + "."));
                        log.info("Game {} - {} search prevented by {}",
                                gameData.id, playerName, sourceName);
                        return false;
                    }
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
                            gameLogService.append(gameData, GameLog.text(logMsg));
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

    private UUID resolvingControllerId(GameData gameData) {
        if (gameData.currentlyResolvingControllerId != null) {
            return gameData.currentlyResolvingControllerId;
        }
        StackEntry pendingEntry = gameData.pendingEffectResolutionEntry;
        return pendingEntry == null ? null : pendingEntry.getControllerId();
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

    /** Returns whether {@code card} carries a permission to be cast during its owner's library search. */
    public boolean isLibrarySearchCastableCard(Card card) {
        return card.getEffects(EffectSlot.STATIC).stream()
                .anyMatch(LibrarySearchCastPermission.class::isInstance);
    }

    /** Returns the cards in {@code playerId}'s library that may be cast during that search. */
    public List<Card> librarySearchCastableCards(GameData gameData, UUID playerId) {
        List<Card> deck = gameData.playerDecks.get(playerId);
        if (deck == null) return List.of();
        return deck.stream().filter(this::isLibrarySearchCastableCard).toList();
    }

    public void sendLibrarySearchToPlayer(GameData gameData, UUID playerId, LibrarySearchParams params,
                                            String prompt, boolean canFailToFind) {
        String playerName = gameData.playerIdToName.get(playerId);
        sendLibrarySearchToPlayer(gameData, playerId, params, prompt, canFailToFind,
                playerName + " searches their library.");
    }

    public void sendLibrarySearchToPlayer(GameData gameData, UUID playerId, LibrarySearchParams params,
                                            String prompt, boolean canFailToFind, String logMessage) {
        // Universal choke point for every library search that presents cards: fire
        // ON_OPPONENT_SEARCHES_LIBRARY (Ob Nixilis, Unshackled) for a player searching their OWN
        // library. A search of someone else's library (targetPlayerId set) is not "their library".
        if (params.targetPlayerId() == null || params.targetPlayerId().equals(params.playerId())) {
            LibrarySearchTriggerHelper.checkOpponentSearchTriggers(gameData, gameLogService, params.playerId());
        }

        // Aven Mindcensor & friends: an opponent's search is limited to the top N cards of that library.
        int topLimit = opponentSearchTopCardsLimit(gameData, params.playerId());
        if (topLimit != Integer.MAX_VALUE) {
            UUID libraryOwnerId = params.targetPlayerId() != null ? params.targetPlayerId() : params.playerId();
            List<Card> restricted = restrictToTopCards(gameData, libraryOwnerId, params.cards(), topLimit);
            if (restricted.isEmpty()) {
                // None of the top N cards match the search: the player searched but found nothing.
                gameLogService.append(gameData, GameLog.text(logMessage));
                if (params.shuffleAfterSelection()) {
                    LibraryShuffleHelper.shuffleLibrary(gameData, libraryOwnerId);
                }
                String searcherName = gameData.playerIdToName.get(params.playerId());
                gameLogService.append(gameData, GameLog.text(
                        searcherName + " finds no matching card among the top " + topLimit
                                + " cards. Library is shuffled."));
                return;
            }
            params = params.withCards(restricted);
        }

        boolean ownLibrarySearch = (params.targetPlayerId() == null || params.targetPlayerId().equals(playerId))
                && !params.sourceSideboard()
                && params.sourceCards() == null;
        if (ownLibrarySearch) {
            params = params.withAllowCastFromLibraryWhileSearching(true);
            List<Card> castableCards = librarySearchCastableCards(gameData, playerId);
            if (!castableCards.isEmpty()) {
                Set<UUID> existingCardIds = params.cards().stream().map(Card::getId).collect(java.util.stream.Collectors.toSet());
                List<Card> cards = new ArrayList<>(params.cards());
                castableCards.stream()
                        .filter(card -> !existingCardIds.contains(card.getId()))
                        .forEach(cards::add);
                if (cards.size() > params.cards().size()) {
                    params = params.withCards(cards);
                    prompt += " You may also cast a card with a library-search permission.";
                }
            }
        }

        interactionHandlerRegistry.begin(gameData, new com.github.laxika.magicalvibes.model.PendingInteraction.LibrarySearch(
                params, prompt, canFailToFind));

        gameLogService.append(gameData, GameLog.text(logMessage));
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
        UUID causingControllerId = resolvingControllerId(gameData);
        return isSearchPrevented(gameData, searchingPlayerId, searchingPlayerId, true, causingControllerId);
    }

    public boolean isSearchPrevented(GameData gameData, UUID searchingPlayerId, boolean shuffleWhenPrevented) {
        UUID causingControllerId = resolvingControllerId(gameData);
        return isSearchPrevented(gameData, searchingPlayerId, searchingPlayerId, shuffleWhenPrevented,
                causingControllerId);
    }

    public boolean isSearchPrevented(GameData gameData, UUID searchingPlayerId,
                                     UUID causingControllerId) {
        return isSearchPrevented(gameData, searchingPlayerId, searchingPlayerId, true, causingControllerId);
    }

    public boolean isSearchPrevented(GameData gameData, UUID searchingPlayerId, UUID libraryOwnerId,
                                     boolean shuffleWhenPrevented, UUID causingControllerId) {
        if (checkSearchRestriction(gameData, searchingPlayerId, libraryOwnerId, causingControllerId)) return false;
        List<Card> deck = gameData.playerDecks.get(libraryOwnerId);
        if (shuffleWhenPrevented && deck != null) {
            LibraryShuffleHelper.shuffleLibrary(gameData, libraryOwnerId);
        }
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
        gameLogService.append(gameData, GameLog.text(logMsg));
    }

    public static String pluralCards(int count) {
        return count + " card" + (count != 1 ? "s" : "");
    }
}
