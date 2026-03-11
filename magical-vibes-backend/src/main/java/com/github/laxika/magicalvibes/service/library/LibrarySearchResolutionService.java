package com.github.laxika.magicalvibes.service.library;

import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.effect.HandlesEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CantSearchLibrariesEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForBasicLandsToBattlefieldTappedAndHandEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DistantMemoriesEffect;
import com.github.laxika.magicalvibes.model.effect.HeadGamesEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaAndSearchLibraryForCardNamedToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForBasicLandToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardTypeToExileAndImprintEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardTypesToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardTypesToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCreatureToTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCreatureWithColorAndMVXOrLessToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCreatureWithExactMVToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCreatureWithMVXOrLessToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SearchTargetLibraryForCardToExileWithPlayPermissionEffect;
import com.github.laxika.magicalvibes.model.effect.SearchTargetLibraryForCardsToGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCreatureWithSubtypeToBattlefieldEffect;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.ChooseCardFromLibraryMessage;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Predicate;

/**
 * Resolves library search effects during stack resolution.
 *
 * <p>Handles effects that let a player search their (or an opponent's) library for
 * specific cards and put them into hand, onto the battlefield, into exile, or on
 * top of the library. Includes card-specific search variants like Distant Memories
 * and Head Games.
 *
 * <p>Library searches respect the Leonin Arbiter search tax via {@link #checkSearchRestriction}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LibrarySearchResolutionService {

    private final DrawService drawService;
    private final GameBroadcastService gameBroadcastService;
    private final SessionManager sessionManager;
    private final CardViewFactory cardViewFactory;

    /**
     * Searches the controller's library for a basic land card and puts it into their hand.
     * The chosen card is revealed. Library is shuffled afterward.
     */
    @HandlesEffect(SearchLibraryForBasicLandToHandEffect.class)
    void resolveSearchLibraryForBasicLandToHand(GameData gameData, StackEntry entry, SearchLibraryForBasicLandToHandEffect effect) {
        performLibrarySearch(
                gameData,
                entry.getControllerId(),
                card -> card.getType() == CardType.LAND && card.getSupertypes().contains(CardSupertype.BASIC),
                "basic land cards",
                "Search your library for a basic land card to put into your hand.",
                true,
                true,
                LibrarySearchDestination.HAND
        );
    }

    /**
     * Searches the controller's library for a card matching the specified types and optional
     * mana value range, then reveals it and puts it into their hand. Used by cards like Sylvan Scrying.
     */
    @HandlesEffect(SearchLibraryForCardTypesToHandEffect.class)
    void resolveSearchLibraryForCardTypesToHand(GameData gameData, StackEntry entry, SearchLibraryForCardTypesToHandEffect effect) {
        Set<CardType> requestedTypes = effect.cardTypes();
        String requestedTypeText = formatCardTypeSetForPrompt(requestedTypes);
        int minMV = effect.minManaValue();
        int maxMV = effect.maxManaValue();

        String mvSuffix;
        if (minMV > 0 && maxMV < Integer.MAX_VALUE) {
            mvSuffix = " with mana value between " + minMV + " and " + maxMV;
        } else if (minMV > 0) {
            mvSuffix = " with mana value " + minMV + " or greater";
        } else if (maxMV < Integer.MAX_VALUE) {
            mvSuffix = " with mana value " + maxMV + " or less";
        } else {
            mvSuffix = "";
        }

        performLibrarySearch(
                gameData,
                entry.getControllerId(),
                card -> {
                    return matchesCardTypes(card, requestedTypes) && card.getManaValue() >= minMV && card.getManaValue() <= maxMV;
                },
                requestedTypeText + " cards" + mvSuffix,
                "Search your library for a " + requestedTypeText + " card" + mvSuffix + " to reveal and put into your hand.",
                true,
                true,
                LibrarySearchDestination.HAND
        );
    }

    /**
     * Searches the controller's library for a card matching the specified types and puts it
     * onto the battlefield (optionally tapped). Used by cards like Rampant Growth and Cultivate.
     */
    @HandlesEffect(SearchLibraryForCardTypesToBattlefieldEffect.class)
    void resolveSearchLibraryForCardTypesToBattlefield(GameData gameData, StackEntry entry,
                                                       SearchLibraryForCardTypesToBattlefieldEffect effect) {
        boolean basicLandOnly = isBasicLandOnlyFilter(effect);
        String filterText = basicLandOnly ? "basic land card" : "matching card";
        String noMatchDesc = basicLandOnly ? "basic land cards" : "matching cards";
        LibrarySearchDestination destination = effect.entersTapped()
                ? LibrarySearchDestination.BATTLEFIELD_TAPPED
                : LibrarySearchDestination.BATTLEFIELD;
        String prompt = effect.entersTapped()
                ? "Search your library for a " + filterText + " and put it onto the battlefield tapped."
                : "Search your library for a " + filterText + " and put it onto the battlefield.";

        performLibrarySearch(
                gameData,
                entry.getControllerId(),
                card -> {
                    if (!matchesCardTypes(card, effect.cardTypes())) return false;
                    return !effect.requiresBasicSupertype() || card.getSupertypes().contains(CardSupertype.BASIC);
                },
                noMatchDesc,
                prompt,
                false,
                true,
                destination
        );
    }

    /**
     * Searches the controller's library for up to two basic land cards, reveals them,
     * puts one onto the battlefield tapped and the other into the controller's hand,
     * then shuffles. The search is a single action (one Leonin Arbiter check).
     *
     * <p>Implemented as two sequential picks: first for battlefield tapped (no shuffle),
     * then for hand (with shuffle). The pending follow-up is stored on
     * {@link GameData#pendingBasicLandToHandSearch} and handled by
     * {@link com.github.laxika.magicalvibes.service.input.LibraryChoiceHandlerService}.
     */
    @HandlesEffect(SearchLibraryForBasicLandsToBattlefieldTappedAndHandEffect.class)
    void resolveCultivate(GameData gameData, StackEntry entry, SearchLibraryForBasicLandsToBattlefieldTappedAndHandEffect effect) {
        UUID controllerId = entry.getControllerId();
        if (isSearchPrevented(gameData, controllerId)) return;

        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);

        if (deck == null || deck.isEmpty()) {
            String logMsg = playerName + " searches their library but it is empty. Library is shuffled.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            return;
        }

        List<Card> basicLands = deck.stream()
                .filter(card -> card.getType() == CardType.LAND && card.getSupertypes().contains(CardSupertype.BASIC))
                .toList();

        if (basicLands.isEmpty()) {
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            String logMsg = playerName + " searches their library but finds no basic land cards. Library is shuffled.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            return;
        }

        // Mark that a follow-up hand search is pending after the first pick
        gameData.pendingBasicLandToHandSearch = true;

        // First pick: basic land to battlefield tapped (no shuffle yet)
        sendLibrarySearchToPlayer(gameData, controllerId, LibrarySearchParams.builder(controllerId, new ArrayList<>(basicLands))
                .reveals(true)
                .canFailToFind(true)
                .destination(LibrarySearchDestination.BATTLEFIELD_TAPPED)
                .shuffleAfterSelection(false)
                .build(), "Search your library for a basic land card to put onto the battlefield tapped.", true);

        log.info("Game {} - {} searches library for Cultivate ({} basic lands)", gameData.id, playerName, basicLands.size());
    }

    /**
     * Searches the controller's library for a card matching the specified types, exiles it,
     * and imprints it on the source permanent.
     */
    @HandlesEffect(SearchLibraryForCardTypeToExileAndImprintEffect.class)
    void resolveSearchLibraryForCardTypeToExileAndImprint(GameData gameData, StackEntry entry,
                                                          SearchLibraryForCardTypeToExileAndImprintEffect effect) {
        gameData.imprintSourcePermanentId = entry.getSourcePermanentId();

        performLibrarySearch(
                gameData,
                entry.getControllerId(),
                card -> matchesCardTypes(card, effect.cardTypes()),
                "matching cards",
                "Search your library for a land card to exile (imprint).",
                false,
                true,
                LibrarySearchDestination.EXILE_IMPRINT
        );
    }

    /**
     * Performs an unrestricted library search — the controller may choose any card and put it
     * into their hand. Used by cards like Diabolic Tutor.
     */
    @HandlesEffect(SearchLibraryForCardToHandEffect.class)
    void resolveSearchLibraryForCardToHand(GameData gameData, StackEntry entry, SearchLibraryForCardToHandEffect effect) {
        performLibrarySearch(
                gameData,
                entry.getControllerId(),
                card -> true,
                "cards",
                "Search your library for a card to put into your hand.",
                false,
                false,
                LibrarySearchDestination.HAND
        );
    }

    /**
     * Searches target opponent's library for up to N cards of the given types and puts them
     * into that player's graveyard. Used by cards like Life's Finale.
     */
    @HandlesEffect(SearchTargetLibraryForCardsToGraveyardEffect.class)
    void resolveSearchTargetLibraryForCardsToGraveyard(GameData gameData, StackEntry entry,
                                                       SearchTargetLibraryForCardsToGraveyardEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID targetPlayerId = entry.getTargetPermanentId();

        if (isSearchPrevented(gameData, controllerId)) return;

        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        String controllerName = gameData.playerIdToName.get(controllerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);

        if (deck == null || deck.isEmpty()) {
            String logMsg = controllerName + " searches " + targetName + "'s library but it is empty. Library is shuffled.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            return;
        }

        List<Card> matchingCards = deck.stream()
                .filter(card -> matchesCardTypes(card, effect.cardTypes()))
                .toList();

        if (matchingCards.isEmpty()) {
            LibraryShuffleHelper.shuffleLibrary(gameData, targetPlayerId);
            String logMsg = controllerName + " searches " + targetName + "'s library but finds no matching cards. Library is shuffled.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            return;
        }

        String prompt = "Search " + targetName + "'s library for a creature card to put into their graveyard (" + effect.maxCount() + " remaining).";
        sendLibrarySearchToPlayer(gameData, controllerId, LibrarySearchParams.builder(controllerId, new ArrayList<>(matchingCards))
                .targetPlayerId(targetPlayerId)
                .remainingCount(effect.maxCount())
                .canFailToFind(true)
                .destination(LibrarySearchDestination.GRAVEYARD)
                .filterCardTypes(effect.cardTypes())
                .build(), prompt, true, controllerName + " searches " + targetName + "'s library.");
    }

    /**
     * Searches target opponent's library for any card, exiles it face down, shuffles,
     * and grants the caster permission to play the exiled card.
     */
    @HandlesEffect(SearchTargetLibraryForCardToExileWithPlayPermissionEffect.class)
    void resolveSearchTargetLibraryForCardToExileWithPlayPermission(GameData gameData, StackEntry entry) {
        UUID controllerId = entry.getControllerId();
        UUID targetPlayerId = entry.getTargetPermanentId();

        if (isSearchPrevented(gameData, controllerId)) return;

        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        String controllerName = gameData.playerIdToName.get(controllerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);

        if (deck == null || deck.isEmpty()) {
            String logMsg = controllerName + " searches " + targetName + "'s library but it is empty. Library is shuffled.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            return;
        }

        List<Card> allCards = new ArrayList<>(deck);

        String prompt = "Search " + targetName + "'s library for a card to exile face down.";
        sendLibrarySearchToPlayer(gameData, controllerId, LibrarySearchParams.builder(controllerId, allCards)
                .targetPlayerId(targetPlayerId)
                .destination(LibrarySearchDestination.EXILE_PLAYABLE)
                .build(), prompt, false, controllerName + " searches " + targetName + "'s library.");

        log.info("Game {} - {} searching {}'s library for Praetor's Grasp ({} cards in library)",
                gameData.id, controllerName, targetName, allCards.size());
    }

    /**
     * Resolves Distant Memories: the controller searches their library for a card and exiles it.
     * Each opponent may then choose to let the controller draw three cards or not.
     * If the library is empty, the controller draws three cards immediately.
     */
    @HandlesEffect(DistantMemoriesEffect.class)
    void resolveDistantMemories(GameData gameData, StackEntry entry) {
        UUID controllerId = entry.getControllerId();
        if (isSearchPrevented(gameData, controllerId)) return;

        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);

        if (deck == null || deck.isEmpty()) {
            // Empty library: no card to exile, but the "if no player does" clause still triggers — draw 3
            String logMsg = playerName + " searches their library but it is empty. " + playerName + " draws three cards.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            for (int i = 0; i < 3; i++) {
                drawService.resolveDrawCard(gameData, controllerId);
            }
            return;
        }

        List<Card> allCards = new ArrayList<>(deck);

        gameData.pendingOpponentExileChoice = new com.github.laxika.magicalvibes.model.PendingOpponentExileChoice(controllerId, 3);

        sendLibrarySearchToPlayer(gameData, controllerId, LibrarySearchParams.builder(controllerId, allCards)
                .destination(LibrarySearchDestination.EXILE)
                .build(), "Search your library for a card to exile.", false);

        log.info("Game {} - {} searching library for Distant Memories ({} cards in library)", gameData.id, playerName, allCards.size());
    }

    /**
     * Searches the controller's library for a creature card with mana value X or less,
     * reveals it, and puts it into their hand. X is determined by the stack entry's X value.
     */
    @HandlesEffect(SearchLibraryForCreatureWithMVXOrLessToHandEffect.class)
    void resolveSearchLibraryForCreatureWithMVXOrLessToHand(GameData gameData, StackEntry entry, SearchLibraryForCreatureWithMVXOrLessToHandEffect effect) {
        int maxMV = entry.getXValue();

        performLibrarySearch(
                gameData,
                entry.getControllerId(),
                card -> card.getType() == CardType.CREATURE && card.getManaValue() <= maxMV,
                "creature card with mana value " + maxMV + " or less",
                "Search your library for a creature card with mana value " + maxMV + " or less to reveal and put into your hand.",
                true,
                true,
                LibrarySearchDestination.HAND
        );
    }

    /**
     * Searches the controller's library for a creature card, reveals it,
     * then shuffles and puts that card on top of their library.
     * Used by cards like Brutalizer Exarch.
     */
    @HandlesEffect(SearchLibraryForCreatureToTopOfLibraryEffect.class)
    void resolveSearchLibraryForCreatureToTopOfLibrary(GameData gameData, StackEntry entry, SearchLibraryForCreatureToTopOfLibraryEffect effect) {
        performLibrarySearch(
                gameData,
                entry.getControllerId(),
                card -> card.getType() == CardType.CREATURE,
                "creature cards",
                "Search your library for a creature card, reveal it, then shuffle and put that card on top.",
                true,
                true,
                LibrarySearchDestination.TOP_OF_LIBRARY
        );
    }

    /**
     * Searches the controller's library for a creature card of the required color with mana value
     * X or less and puts it onto the battlefield. X is determined by the stack entry's X value.
     */
    @HandlesEffect(SearchLibraryForCreatureWithColorAndMVXOrLessToBattlefieldEffect.class)
    void resolveSearchLibraryForCreatureWithColorAndMVXOrLessToBattlefield(GameData gameData, StackEntry entry,
                                                                            SearchLibraryForCreatureWithColorAndMVXOrLessToBattlefieldEffect effect) {
        int maxMV = entry.getXValue();
        String colorName = effect.requiredColor().name().toLowerCase();

        performLibrarySearch(
                gameData,
                entry.getControllerId(),
                card -> card.getType() == CardType.CREATURE
                        && card.getColors().contains(effect.requiredColor())
                        && card.getManaValue() <= maxMV,
                colorName + " creature card with mana value " + maxMV + " or less",
                "Search your library for a " + colorName + " creature card with mana value " + maxMV + " or less and put it onto the battlefield.",
                false,
                true,
                LibrarySearchDestination.BATTLEFIELD
        );
    }

    /**
     * Searches the controller's library for a creature card with the required subtype
     * and puts it onto the battlefield.
     */
    @HandlesEffect(SearchLibraryForCreatureWithSubtypeToBattlefieldEffect.class)
    void resolveSearchLibraryForCreatureWithSubtypeToBattlefield(GameData gameData, StackEntry entry,
                                                                  SearchLibraryForCreatureWithSubtypeToBattlefieldEffect effect) {
        CardSubtype requiredSubtype = effect.requiredSubtype();
        String subtypeName = requiredSubtype.name().substring(0, 1) + requiredSubtype.name().substring(1).toLowerCase();

        performLibrarySearch(
                gameData,
                entry.getControllerId(),
                card -> {
                    boolean isCreatureCard = card.getType() == CardType.CREATURE
                            || card.getAdditionalTypes().contains(CardType.CREATURE);
                    return isCreatureCard && card.getSubtypes().contains(requiredSubtype);
                },
                subtypeName + " creature card",
                "Search your library for a " + subtypeName + " creature card and put it onto the battlefield.",
                false,
                true,
                LibrarySearchDestination.BATTLEFIELD
        );
    }

    @HandlesEffect(SearchLibraryForCreatureWithExactMVToBattlefieldEffect.class)
    void resolveSearchLibraryForCreatureWithExactMVToBattlefield(GameData gameData, StackEntry entry,
                                                                 SearchLibraryForCreatureWithExactMVToBattlefieldEffect effect) {
        int targetMV = entry.getXValue() + effect.mvOffset();

        performLibrarySearch(
                gameData,
                entry.getControllerId(),
                card -> (card.getType() == CardType.CREATURE || card.getAdditionalTypes().contains(CardType.CREATURE))
                        && card.getManaValue() == targetMV,
                "creature card with mana value " + targetMV,
                "Search your library for a creature card with mana value " + targetMV + " and put it onto the battlefield.",
                false,
                true,
                LibrarySearchDestination.BATTLEFIELD
        );
    }

    /**
     * Pays a mana cost and then searches the controller's library for a card with the specified
     * name, putting it onto the battlefield. If the mana cannot be paid, the effect does nothing.
     */
    @HandlesEffect(PayManaAndSearchLibraryForCardNamedToBattlefieldEffect.class)
    void resolvePayManaAndSearchLibraryForCardNamedToBattlefield(GameData gameData, StackEntry entry,
                                                                  PayManaAndSearchLibraryForCardNamedToBattlefieldEffect effect) {
        UUID controllerId = entry.getControllerId();
        if (isSearchPrevented(gameData, controllerId)) return;

        String playerName = gameData.playerIdToName.get(controllerId);
        ManaCost cost = new ManaCost(effect.manaCost());
        if (!cost.canPay(gameData.playerManaPools.get(controllerId))) {
            String logMsg = playerName + " can't pay " + effect.manaCost() + ".";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            return;
        }

        cost.pay(gameData.playerManaPools.get(controllerId));

        performLibrarySearch(
                gameData,
                controllerId,
                card -> effect.cardName().equals(card.getName()),
                effect.cardName(),
                "Search your library for a card named " + effect.cardName() + " and put it onto the battlefield.",
                false,
                true,
                LibrarySearchDestination.BATTLEFIELD
        );
    }

    /**
     * Resolves Head Games: the target opponent puts their hand on top of their library,
     * then the caster searches that library and chooses cards equal to the former hand size
     * to become the target's new hand. Library is shuffled afterward.
     */
    @HandlesEffect(HeadGamesEffect.class)
    void resolveHeadGames(GameData gameData, StackEntry entry, HeadGamesEffect effect) {
        UUID casterId = entry.getControllerId();
        UUID targetPlayerId = entry.getTargetPermanentId();
        List<Card> targetHand = gameData.playerHands.get(targetPlayerId);
        List<Card> targetDeck = gameData.playerDecks.get(targetPlayerId);
        String casterName = gameData.playerIdToName.get(casterId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);

        // Check search restriction — if unpaid Arbiters exist, search is prevented.
        if (!checkSearchRestriction(gameData, casterId)) {
            // Search prevented — still execute remaining spell steps per rules:
            // target puts hand on top of library, then library is shuffled.
            putHandOnTopOfLibrary(gameData, targetHand, targetDeck, targetName);
            Collections.shuffle(targetDeck);
            String shuffleLog = targetName + "'s library is shuffled.";
            gameBroadcastService.logAndBroadcast(gameData, shuffleLog);
            return;
        }

        int handSize = targetHand.size();

        // Step 1: Target opponent puts hand on top of library
        if (handSize == 0) {
            String logMsg = targetName + " has no cards in hand. " + targetName + "'s library is shuffled.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            Collections.shuffle(targetDeck);
            return;
        }

        putHandOnTopOfLibrary(gameData, targetHand, targetDeck, targetName);

        // Step 2: Caster searches target's library for that many cards
        List<Card> allCards = new ArrayList<>(targetDeck);

        String prompt = "Search " + targetName + "'s library for a card to put into their hand (" + handSize + " remaining).";
        sendLibrarySearchToPlayer(gameData, casterId, LibrarySearchParams.builder(casterId, allCards)
                .targetPlayerId(targetPlayerId)
                .remainingCount(handSize)
                .build(), prompt, false, casterName + " searches " + targetName + "'s library.");
        log.info("Game {} - {} resolving Head Games, searching {}'s library for {} cards",
                gameData.id, casterName, targetName, handSize);
    }

    // =========================================================================
    // Shared search infrastructure
    // =========================================================================

    /**
     * Unified library search skeleton: check restriction -> get deck -> filter -> handle no matches ->
     * begin interaction -> send message -> log. Returns true if the search was initiated, false otherwise.
     */
    private boolean performLibrarySearch(
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
    boolean checkSearchRestriction(GameData gameData, UUID searchingPlayerId) {
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

    private String formatCardTypeSetForPrompt(Set<CardType> cardTypes) {
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

    private boolean isBasicLandOnlyFilter(SearchLibraryForCardTypesToBattlefieldEffect effect) {
        return effect.requiresBasicSupertype()
                && effect.cardTypes().size() == 1
                && effect.cardTypes().contains(CardType.LAND);
    }

    private static boolean matchesCardTypes(Card card, Set<CardType> cardTypes) {
        return cardTypes.contains(card.getType())
                || card.getAdditionalTypes().stream().anyMatch(cardTypes::contains);
    }

    private void sendLibrarySearchToPlayer(GameData gameData, UUID playerId, LibrarySearchParams params,
                                            String prompt, boolean canFailToFind) {
        String playerName = gameData.playerIdToName.get(playerId);
        sendLibrarySearchToPlayer(gameData, playerId, params, prompt, canFailToFind,
                playerName + " searches their library.");
    }

    private void sendLibrarySearchToPlayer(GameData gameData, UUID playerId, LibrarySearchParams params,
                                            String prompt, boolean canFailToFind, String logMessage) {
        gameData.interaction.beginLibrarySearch(params);

        List<CardView> cardViews = params.cards().stream().map(cardViewFactory::create).toList();
        sessionManager.sendToPlayer(playerId, new ChooseCardFromLibraryMessage(cardViews, prompt, canFailToFind));

        gameBroadcastService.logAndBroadcast(gameData, logMessage);
    }

    private boolean isSearchPrevented(GameData gameData, UUID searchingPlayerId) {
        if (checkSearchRestriction(gameData, searchingPlayerId)) return false;
        List<Card> deck = gameData.playerDecks.get(searchingPlayerId);
        if (deck != null) LibraryShuffleHelper.shuffleLibrary(gameData, searchingPlayerId);
        return true;
    }

    private void putHandOnTopOfLibrary(GameData gameData, List<Card> hand, List<Card> deck, String playerName) {
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

    private static String pluralCards(int count) {
        return count + " card" + (count != 1 ? "s" : "");
    }
}
