package com.github.laxika.magicalvibes.service.library;

import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameHelper;
import com.github.laxika.magicalvibes.service.effect.HandlesEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AjaniUltimateEffect;
import com.github.laxika.magicalvibes.model.effect.CantSearchLibrariesEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastTopOfLibraryWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.effect.DistantMemoriesEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentMillsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsRepeatOnDuplicateEffect;
import com.github.laxika.magicalvibes.model.effect.HeadGamesEffect;
import com.github.laxika.magicalvibes.model.effect.ImprintFromTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsHandTopBottomEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsMayRevealCreaturePutIntoHandRestOnBottomEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsPutMatchingPermanentNameOnBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.MillByHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.MillHalfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerByChargeCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaAndSearchLibraryForCardNamedToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReorderTopCardsOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForBasicLandToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardTypeToExileAndImprintEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardTypesToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardTypesToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCreatureWithColorAndMVXOrLessToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCreatureWithExactMVToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCreatureWithSubtypeToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCreatureToTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCreatureWithMVXOrLessToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryEffect;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.ChooseCardFromLibraryMessage;
import com.github.laxika.magicalvibes.networking.message.ChooseHandTopBottomMessage;
import com.github.laxika.magicalvibes.networking.message.ChooseMultipleCardsFromGraveyardsMessage;
import com.github.laxika.magicalvibes.networking.message.ReorderLibraryCardsMessage;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * Resolves all library-related card effects during stack resolution.
 *
 * <p>Handles milling, shuffling, searching, revealing, reordering, and imprinting
 * from player libraries. Each public/package-private method is dispatched via
 * {@link HandlesEffect} annotations from the effect resolution framework.
 *
 * <p>Library searches respect the Leonin Arbiter search tax via {@link #checkSearchRestriction}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LibraryResolutionService {

    private final GameHelper gameHelper;
    private final GameBroadcastService gameBroadcastService;
    private final SessionManager sessionManager;
    private final CardViewFactory cardViewFactory;

    /**
     * Shuffles the spell's card into its owner's library instead of going to the graveyard.
     * Used by cards like Red Sun's Zenith.
     */
    @HandlesEffect(ShuffleIntoLibraryEffect.class)
    void resolveShuffleIntoLibrary(GameData gameData, StackEntry entry) {
        List<Card> deck = gameData.playerDecks.get(entry.getControllerId());
        deck.add(entry.getCard());
        Collections.shuffle(deck);

        String shuffleLog = entry.getCard().getName() + " is shuffled into its owner's library.";
        gameBroadcastService.logAndBroadcast(gameData, shuffleLog);
    }

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

        gameHelper.resolveMillPlayer(gameData, targetPlayerId, handSize);
    }

    /**
     * Mills the target player for a fixed number of cards specified by the effect.
     */
    @HandlesEffect(MillTargetPlayerEffect.class)
    void resolveMillTargetPlayer(GameData gameData, StackEntry entry, MillTargetPlayerEffect mill) {
        gameHelper.resolveMillPlayer(gameData, entry.getTargetPermanentId(), mill.count());
    }

    /**
     * Mills each opponent for a fixed number of cards.
     */
    @HandlesEffect(EachOpponentMillsEffect.class)
    void resolveEachOpponentMills(GameData gameData, StackEntry entry, EachOpponentMillsEffect effect) {
        UUID controllerId = entry.getControllerId();
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(controllerId)) continue;
            gameHelper.resolveMillPlayer(gameData, playerId, effect.count());
        }
    }

    /**
     * Exiles cards from the top of the target player's library one at a time,
     * repeating until a card with a duplicate name is exiled.
     */
    @HandlesEffect(ExileTopCardsRepeatOnDuplicateEffect.class)
    void resolveExileTopCardsRepeatOnDuplicate(GameData gameData, StackEntry entry, ExileTopCardsRepeatOnDuplicateEffect effect) {
        UUID targetPlayerId = entry.getTargetPermanentId();
        gameHelper.resolveExileTopCardsRepeatOnDuplicate(gameData, entry.getCard(), targetPlayerId, effect);
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

        gameHelper.resolveMillPlayer(gameData, targetPlayerId, chargeCounters);
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

        gameHelper.resolveMillPlayer(gameData, targetPlayerId, cardsToMill);
    }

    /**
     * Shuffles the target player's entire graveyard into their library.
     * Used by cards like Reminisce.
     */
    @HandlesEffect(ShuffleGraveyardIntoLibraryEffect.class)
    void resolveShuffleGraveyardIntoLibrary(GameData gameData, StackEntry entry) {
        UUID targetPlayerId = entry.getTargetPermanentId();
        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        List<Card> graveyard = gameData.playerGraveyards.get(targetPlayerId);
        String playerName = gameData.playerIdToName.get(targetPlayerId);

        if (graveyard.isEmpty()) {
            String logEntry = playerName + "'s graveyard is empty. Library is shuffled.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            Collections.shuffle(deck);
            return;
        }

        int count = graveyard.size();
        deck.addAll(graveyard);
        graveyard.clear();
        Collections.shuffle(deck);

        String logEntry = playerName + " shuffles their graveyard (" + pluralCards(count) + ") into their library.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} shuffles graveyard ({} cards) into library", gameData.id, playerName, count);
    }

    /**
     * Reveals the top card of the target player's library to all players without removing it.
     * Used by cards like Aven Windreader.
     */
    @HandlesEffect(RevealTopCardOfLibraryEffect.class)
    void resolveRevealTopCardOfLibrary(GameData gameData, StackEntry entry) {
        UUID targetPlayerId = entry.getTargetPermanentId();
        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        String playerName = gameData.playerIdToName.get(targetPlayerId);

        if (deck.isEmpty()) {
            String logEntry = playerName + "'s library is empty.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
        } else {
            Card topCard = deck.getFirst();
            String logEntry = playerName + " reveals " + topCard.getName() + " from the top of their library.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
        }

        log.info("Game {} - {} reveals top card of library", gameData.id, playerName);
    }

    /**
     * Looks at the top card of the controller's library and, if it matches the castable types,
     * queues a "may" ability to cast it without paying its mana cost.
     */
    @HandlesEffect(CastTopOfLibraryWithoutPayingManaCostEffect.class)
    void resolveCastTopOfLibraryWithoutPaying(GameData gameData, StackEntry entry, CastTopOfLibraryWithoutPayingManaCostEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();

        if (deck.isEmpty()) {
            String logEntry = playerName + "'s library is empty (" + sourceName + ").";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        Card topCard = deck.getFirst();

        String logEntry = playerName + " looks at the top card of their library (" + sourceName + ").";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} looks at top card: {} ({})", gameData.id, playerName, topCard.getName(), sourceName);

        boolean matches = effect.castableTypes().contains(topCard.getType());
        if (!matches) {
            String noMatch = "The top card is not a castable type.";
            gameBroadcastService.logAndBroadcast(gameData, noMatch);
            log.info("Game {} - Top card {} doesn't match castable types", gameData.id, topCard.getName());
            return;
        }

        // Card matches — queue may ability to cast the spell without paying mana cost
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                topCard,
                controllerId,
                List.of(effect),
                sourceName + " — Cast " + topCard.getName() + " without paying its mana cost?"
        ));
    }

    /**
     * Lets the controller look at the top N cards of their library and reorder them.
     * If only one card remains, the player simply looks at it. Used by cards like Sage Owl.
     */
    @HandlesEffect(ReorderTopCardsOfLibraryEffect.class)
    void resolveReorderTopCardsOfLibrary(GameData gameData, StackEntry entry, ReorderTopCardsOfLibraryEffect reorder) {
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);

        int count = Math.min(reorder.count(), deck.size());
        if (count == 0) {
            String logMsg = entry.getCard().getName() + ": library is empty, nothing to reorder.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            return;
        }

        if (count == 1) {
            String logMsg = gameData.playerIdToName.get(controllerId) + " looks at the top card of their library.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            return;
        }

        List<Card> topCards = new ArrayList<>(deck.subList(0, count));

        gameData.interaction.beginLibraryReorder(controllerId, topCards, false);

        List<CardView> cardViews = topCards.stream().map(cardViewFactory::create).toList();
        sessionManager.sendToPlayer(controllerId, new ReorderLibraryCardsMessage(
                cardViews,
                "Put these cards back on top of your library in any order (top to bottom)."
        ));

        String logMsg = gameData.playerIdToName.get(controllerId) + " looks at the top " + count + " cards of their library.";
        gameBroadcastService.logAndBroadcast(gameData, logMsg);
        log.info("Game {} - {} reordering top {} cards of library", gameData.id, gameData.playerIdToName.get(controllerId), count);
    }

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

    private boolean isBasicLandOnlyFilter(SearchLibraryForCardTypesToBattlefieldEffect effect) {
        return effect.requiresBasicSupertype()
                && effect.cardTypes().size() == 1
                && effect.cardTypes().contains(CardType.LAND);
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
                gameHelper.resolveDrawCard(gameData, controllerId);
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

    /**
     * Resolves Ajani Goldmane's ultimate: look at the top cards of your library equal to your
     * life total, then put any number of nonland permanent cards with mana value 3 or less
     * onto the battlefield. The rest are shuffled back.
     */
    @HandlesEffect(AjaniUltimateEffect.class)
    void resolveAjaniUltimate(GameData gameData, StackEntry entry) {
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        int lifeTotal = gameData.playerLifeTotals.getOrDefault(controllerId, 20);

        int count = Math.min(lifeTotal, deck.size());
        if (count <= 0) {
            String logMsg = playerName + " looks at no cards (library is empty or life total is 0). Library is shuffled.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            Collections.shuffle(deck);
            return;
        }

        // Take top X cards from library
        List<Card> revealedCards = takeTopCards(deck, count);

        // Filter to nonland permanent cards with MV ≤ 3
        List<Card> eligibleCards = new ArrayList<>();
        for (Card card : revealedCards) {
            if (card.getType() != null
                    && card.getType() != CardType.LAND
                    && card.getType() != CardType.INSTANT
                    && card.getType() != CardType.SORCERY
                    && card.getManaValue() <= 3) {
                eligibleCards.add(card);
            }
        }

        String logMsg = playerName + " looks at the top " + count + " cards of their library.";
        gameBroadcastService.logAndBroadcast(gameData, logMsg);

        if (eligibleCards.isEmpty()) {
            // No eligible cards — put all back and shuffle
            deck.addAll(revealedCards);
            Collections.shuffle(deck);
            String shuffleLog = playerName + " finds no eligible cards. Library is shuffled.";
            gameBroadcastService.logAndBroadcast(gameData, shuffleLog);
            return;
        }

        // Set up player choice for selecting cards to put onto battlefield
        Set<UUID> validCardIds = ConcurrentHashMap.newKeySet();
        for (Card card : eligibleCards) {
            validCardIds.add(card.getId());
        }

        gameData.interaction.beginLibraryRevealChoice(controllerId, revealedCards, validCardIds);

        List<CardView> cardViews = eligibleCards.stream().map(cardViewFactory::create).toList();
        List<UUID> cardIds = eligibleCards.stream().map(Card::getId).toList();
        sessionManager.sendToPlayer(controllerId, new ChooseMultipleCardsFromGraveyardsMessage(
                cardIds, cardViews, eligibleCards.size(),
                "Choose any number of nonland permanent cards with mana value 3 or less to put onto the battlefield."
        ));

        log.info("Game {} - {} resolving Ajani ultimate with {} revealed, {} eligible", gameData.id, playerName, count, eligibleCards.size());
    }

    /**
     * Looks at the top N cards of the controller's library, then the player chooses one to put
     * into their hand and puts the rest on the bottom. If only one card remains, it goes
     * directly to hand. Used by cards like Telling Time.
     */
    @HandlesEffect(LookAtTopCardsHandTopBottomEffect.class)
    void resolveLookAtTopCardsHandTopBottom(GameData gameData, StackEntry entry, LookAtTopCardsHandTopBottomEffect effect) {
        TopCardsResult result = takeTopCardsFromLibrary(gameData, entry, effect.count());
        if (result == null) return;
        UUID controllerId = result.controllerId();
        List<Card> topCards = result.topCards();
        String playerName = result.playerName();
        int count = topCards.size();

        if (count == 1) {
            // Only 1 card: it goes to hand
            gameData.playerHands.get(controllerId).add(topCards.getFirst());
            String logMsg = playerName + " looks at the top card of their library and puts it into their hand.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            return;
        }

        gameData.interaction.beginHandTopBottomChoice(controllerId, topCards);

        List<CardView> cardViews = topCards.stream().map(cardViewFactory::create).toList();
        sessionManager.sendToPlayer(controllerId, new ChooseHandTopBottomMessage(
                cardViews,
                "Look at the top " + count + " cards of your library. Choose one to put into your hand."
        ));

        gameBroadcastService.logAndBroadcast(gameData,
                playerName + " looks at the top " + pluralCards(count) + " of their library.");
        log.info("Game {} - {} resolving {} with {} cards", gameData.id, playerName, entry.getCard().getName(), count);
    }

    /**
     * Looks at the top N cards of the controller's library, then the player may reveal one
     * (or any number, if the effect allows) matching creature card(s) and put them into their
     * hand. Remaining cards go to the bottom of the library in any order.
     */
    @HandlesEffect(LookAtTopCardsMayRevealCreaturePutIntoHandRestOnBottomEffect.class)
    void resolveLookAtTopCardsMayRevealCreaturePutIntoHandRestOnBottom(
            GameData gameData,
            StackEntry entry,
            LookAtTopCardsMayRevealCreaturePutIntoHandRestOnBottomEffect effect
    ) {
        TopCardsResult result = takeTopCardsFromLibrary(gameData, entry, effect.count(), true);
        if (result == null) return;
        UUID controllerId = result.controllerId();
        List<Card> topCards = result.topCards();
        int count = topCards.size();

        List<Card> creatureCards = topCards.stream()
                .filter(card -> matchesCardTypes(card, effect.cardTypes()))
                .toList();

        if (creatureCards.isEmpty()) {
            reorderRemainingToBottom(gameData, controllerId, topCards);
            return;
        }

        if (effect.anyNumber()) {
            Set<UUID> validCardIds = ConcurrentHashMap.newKeySet();
            for (Card card : creatureCards) {
                validCardIds.add(card.getId());
            }

            gameData.interaction.beginLibraryRevealChoice(controllerId, topCards, validCardIds,
                    false, true, true);

            List<CardView> cardViews = creatureCards.stream().map(cardViewFactory::create).toList();
            List<UUID> cardIds = creatureCards.stream().map(Card::getId).toList();
            sessionManager.sendToPlayer(controllerId, new ChooseMultipleCardsFromGraveyardsMessage(
                    cardIds, cardViews, creatureCards.size(),
                    "You may reveal any number of creature cards and put them into your hand."
            ));
            return;
        }

        gameData.interaction.beginLibrarySearch(LibrarySearchParams.builder(controllerId, creatureCards)
                .reveals(true)
                .canFailToFind(true)
                .sourceCards(topCards)
                .reorderRemainingToBottom(true)
                .shuffleAfterSelection(false)
                .prompt("You may reveal a creature card from among them and put it into your hand.")
                .build());

        List<CardView> cardViews = creatureCards.stream().map(cardViewFactory::create).toList();
        sessionManager.sendToPlayer(controllerId, new ChooseCardFromLibraryMessage(
                cardViews,
                "You may reveal a creature card from among them and put it into your hand.",
                true
        ));
    }

    /**
     * Looks at the top N cards of the controller's library. The player may put one onto the
     * battlefield if it shares a name with a permanent already on any battlefield. Remaining
     * cards go to the bottom of the library in any order.
     */
    @HandlesEffect(LookAtTopCardsPutMatchingPermanentNameOnBattlefieldEffect.class)
    void resolveLookAtTopCardsPutMatchingPermanentNameOnBattlefield(
            GameData gameData,
            StackEntry entry,
            LookAtTopCardsPutMatchingPermanentNameOnBattlefieldEffect effect
    ) {
        TopCardsResult result = takeTopCardsFromLibrary(gameData, entry, effect.count(), true);
        if (result == null) return;
        UUID controllerId = result.controllerId();
        List<Card> topCards = result.topCards();

        // Collect all permanent names from all battlefields
        Set<String> permanentNames = new HashSet<>();
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf != null) {
                for (Permanent perm : bf) {
                    permanentNames.add(perm.getCard().getName());
                }
            }
        }

        // Filter top cards to those matching a permanent name
        List<Card> matchingCards = topCards.stream()
                .filter(card -> permanentNames.contains(card.getName()))
                .toList();

        if (matchingCards.isEmpty()) {
            reorderRemainingToBottom(gameData, controllerId, topCards);
            return;
        }

        gameData.interaction.beginLibrarySearch(LibrarySearchParams.builder(controllerId, matchingCards)
                .canFailToFind(true)
                .sourceCards(topCards)
                .reorderRemainingToBottom(true)
                .shuffleAfterSelection(false)
                .prompt("You may put one of these cards onto the battlefield.")
                .destination(LibrarySearchDestination.BATTLEFIELD)
                .build());

        List<CardView> cardViews = matchingCards.stream().map(cardViewFactory::create).toList();
        sessionManager.sendToPlayer(controllerId, new ChooseCardFromLibraryMessage(
                cardViews,
                "You may put one of these cards onto the battlefield if it has the same name as a permanent.",
                true
        ));
    }

    /**
     * Looks at the top N cards of the controller's library and exiles one face down (imprint).
     * If only one card is available, it is exiled automatically. Otherwise the player chooses,
     * and remaining cards go to the bottom. Used by cards like Clone Shell.
     */
    @HandlesEffect(ImprintFromTopCardsEffect.class)
    void resolveImprintFromTopCards(GameData gameData, StackEntry entry, ImprintFromTopCardsEffect effect) {
        TopCardsResult result = takeTopCardsFromLibrary(gameData, entry, effect.count(), true);
        if (result == null) return;
        UUID controllerId = result.controllerId();
        List<Card> topCards = result.topCards();
        String playerName = result.playerName();

        if (topCards.size() == 1) {
            // Only one card — must exile it, nothing to reorder
            gameData.playerExiledCards.get(controllerId).add(topCards.getFirst());
            UUID sourcePermanentId = entry.getSourcePermanentId();
            if (sourcePermanentId != null) {
                gameHelper.setImprintedCardOnPermanent(gameData, sourcePermanentId, topCards.getFirst());
            }
            String exileLog = playerName + " exiles a card face down with " + entry.getCard().getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, exileLog);
            return;
        }

        gameData.imprintSourcePermanentId = entry.getSourcePermanentId();

        List<Card> sourceCards = new ArrayList<>(topCards);

        gameData.interaction.beginLibrarySearch(LibrarySearchParams.builder(controllerId, topCards)
                .sourceCards(sourceCards)
                .reorderRemainingToBottom(true)
                .shuffleAfterSelection(false)
                .prompt("Exile one card face down (imprint). The rest go to the bottom of your library.")
                .destination(LibrarySearchDestination.EXILE_IMPRINT)
                .build());

        List<CardView> cardViews = topCards.stream().map(cardViewFactory::create).toList();
        sessionManager.sendToPlayer(controllerId, new ChooseCardFromLibraryMessage(
                cardViews,
                "Exile one card face down (imprint). The rest go to the bottom of your library.",
                false
        ));
    }

    /**
     * Unified library search skeleton: check restriction → get deck → filter → handle no matches →
     * begin interaction → send message → log. Returns true if the search was initiated, false otherwise.
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
            Collections.shuffle(deck);
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

    private static String pluralCards(int count) {
        return count + " card" + (count != 1 ? "s" : "");
    }

    private static boolean matchesCardTypes(Card card, Set<CardType> cardTypes) {
        return cardTypes.contains(card.getType())
                || card.getAdditionalTypes().stream().anyMatch(cardTypes::contains);
    }

    private static List<Card> takeTopCards(List<Card> deck, int count) {
        List<Card> topCards = new ArrayList<>(deck.subList(0, count));
        deck.subList(0, count).clear();
        return topCards;
    }

    private void reorderRemainingToBottom(GameData gameData, UUID controllerId, List<Card> topCards) {
        if (topCards.size() == 1) {
            gameData.playerDecks.get(controllerId).add(topCards.getFirst());
            return;
        }
        gameData.interaction.beginLibraryReorder(controllerId, topCards, true);
        List<CardView> cardViews = topCards.stream().map(cardViewFactory::create).toList();
        sessionManager.sendToPlayer(controllerId, new ReorderLibraryCardsMessage(
                cardViews,
                "Put these cards on the bottom of your library in any order (first chosen will be closest to the top)."
        ));
    }

    private record TopCardsResult(UUID controllerId, List<Card> topCards, String playerName) {}

    private TopCardsResult takeTopCardsFromLibrary(GameData gameData, StackEntry entry, int count) {
        return takeTopCardsFromLibrary(gameData, entry, count, false);
    }

    private TopCardsResult takeTopCardsFromLibrary(GameData gameData, StackEntry entry, int count, boolean broadcastLook) {
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);

        int actual = Math.min(count, deck.size());
        if (actual == 0) {
            String logMsg = entry.getCard().getName() + ": " + playerName + "'s library is empty.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            return null;
        }

        List<Card> topCards = takeTopCards(deck, actual);

        if (broadcastLook) {
            String logMsg = playerName + " looks at the top " + pluralCards(actual) + " of their library.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
        }

        return new TopCardsResult(controllerId, topCards, playerName);
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
        if (deck != null) Collections.shuffle(deck);
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

}


