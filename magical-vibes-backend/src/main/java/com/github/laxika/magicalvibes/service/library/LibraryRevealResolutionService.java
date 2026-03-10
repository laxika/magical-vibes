package com.github.laxika.magicalvibes.service.library;

import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.HandlesEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AjaniUltimateEffect;
import com.github.laxika.magicalvibes.model.effect.CastTopOfLibraryWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.ImprintFromTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsHandTopBottomEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsPerChargeCounterChooseOneToHandRestOnBottomEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsOfTargetLibraryMayExileOneEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsMayRevealCreaturePutIntoHandRestOnBottomEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsPutMatchingPermanentNameOnBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReorderTopCardsOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.ChooseCardFromLibraryMessage;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import com.github.laxika.magicalvibes.networking.message.ChooseHandTopBottomMessage;
import com.github.laxika.magicalvibes.networking.message.ChooseMultipleCardsFromGraveyardsMessage;
import com.github.laxika.magicalvibes.networking.message.ReorderLibraryCardsMessage;
import com.github.laxika.magicalvibes.networking.message.ScryMessage;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resolves library reveal, look-at, and reorder effects during stack resolution.
 *
 * <p>Handles effects that look at or reveal the top cards of a player's library,
 * including simple reveals, reordering, hand/top/bottom choices, creature reveals,
 * permanent-name matching, imprinting from top cards, and Ajani's ultimate.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LibraryRevealResolutionService {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final SessionManager sessionManager;
    private final CardViewFactory cardViewFactory;

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
        deck.subList(0, count).clear();

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
     * Lets the controller look at the top N cards of their library and choose which to put
     * on top (in any order) and which to put on the bottom (in any order). Used by cards
     * with the scry keyword ability.
     */
    @HandlesEffect(ScryEffect.class)
    void resolveScry(GameData gameData, StackEntry entry, ScryEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);

        // 701.22b: If a player is instructed to scry 0, no scry event occurs.
        if (effect.count() == 0) {
            return;
        }

        int count = Math.min(effect.count(), deck.size());

        // 701.22d: Empty library — scry event still occurs (triggers would fire), but nothing to interact with.
        if (count == 0) {
            String logMsg = gameData.playerIdToName.get(controllerId) + " scries " + effect.count()
                    + " but their library is empty.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            return;
        }

        if (count == 1) {
            // Scry 1: show single card, player chooses top or bottom
            List<Card> topCards = new ArrayList<>(deck.subList(0, 1));
            deck.subList(0, 1).clear();

            gameData.interaction.beginScry(controllerId, topCards);

            List<CardView> cardViews = topCards.stream().map(cardViewFactory::create).toList();
            sessionManager.sendToPlayer(controllerId, new ScryMessage(
                    cardViews,
                    "Scry 1: Keep on top or put on the bottom of your library."
            ));

            String logMsg = gameData.playerIdToName.get(controllerId) + " scries 1.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            log.info("Game {} - {} scries 1", gameData.id, gameData.playerIdToName.get(controllerId));
            return;
        }

        List<Card> topCards = new ArrayList<>(deck.subList(0, count));
        deck.subList(0, count).clear();

        gameData.interaction.beginScry(controllerId, topCards);

        List<CardView> cardViews = topCards.stream().map(cardViewFactory::create).toList();
        sessionManager.sendToPlayer(controllerId, new ScryMessage(
                cardViews,
                "Scry " + count + ": Put cards on the top or bottom of your library."
        ));

        String logMsg = gameData.playerIdToName.get(controllerId) + " scries " + count + ".";
        gameBroadcastService.logAndBroadcast(gameData, logMsg);
        log.info("Game {} - {} scries {}", gameData.id, gameData.playerIdToName.get(controllerId), count);
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
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            return;
        }

        // Take top X cards from library
        List<Card> revealedCards = takeTopCards(deck, count);

        // Filter to nonland permanent cards with MV <= 3
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
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
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
     * Looks at the top X cards of the controller's library, where X is the number of charge
     * counters on the source (stored in xValue). The player chooses one to put into their hand
     * and the rest go on the bottom of the library in any order.
     */
    @HandlesEffect(LookAtTopCardsPerChargeCounterChooseOneToHandRestOnBottomEffect.class)
    void resolveLookAtTopCardsPerChargeCounter(GameData gameData, StackEntry entry) {
        int count = entry.getXValue();
        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);

        if (count <= 0) {
            String logMsg = entry.getCard().getName() + ": no charge counters, nothing to look at.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            return;
        }

        List<Card> deck = gameData.playerDecks.get(controllerId);
        int actual = Math.min(count, deck.size());
        if (actual == 0) {
            String logMsg = entry.getCard().getName() + ": " + playerName + "'s library is empty.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            return;
        }

        List<Card> topCards = takeTopCards(deck, actual);

        if (actual == 1) {
            gameData.playerHands.get(controllerId).add(topCards.getFirst());
            String logMsg = playerName + " looks at the top card of their library and puts it into their hand.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            return;
        }

        gameData.interaction.beginHandTopBottomChoice(controllerId, topCards);

        List<CardView> cardViews = topCards.stream().map(cardViewFactory::create).toList();
        sessionManager.sendToPlayer(controllerId, new ChooseHandTopBottomMessage(
                cardViews,
                "Look at the top " + actual + " cards of your library. Choose one to put into your hand."
        ));

        gameBroadcastService.logAndBroadcast(gameData,
                playerName + " looks at the top " + pluralCards(actual) + " of their library.");
        log.info("Game {} - {} resolving {} with {} cards", gameData.id, playerName, entry.getCard().getName(), actual);
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
                gameQueryService.setImprintedCardOnPermanent(gameData, sourcePermanentId, topCards.getFirst());
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
     * Looks at the top N cards of the target player's library. The controller may exile one
     * of those cards. The rest are put on top of that library in any order.
     * Used by Psychic Surgery.
     */
    @HandlesEffect(LookAtTopCardsOfTargetLibraryMayExileOneEffect.class)
    void resolveLookAtTopCardsOfTargetLibraryMayExileOne(
            GameData gameData,
            StackEntry entry,
            LookAtTopCardsOfTargetLibraryMayExileOneEffect effect
    ) {
        UUID controllerId = entry.getControllerId();
        UUID targetPlayerId = entry.getTargetPermanentId();
        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        String controllerName = gameData.playerIdToName.get(controllerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);

        int actual = Math.min(effect.count(), deck.size());
        if (actual == 0) {
            String logMsg = entry.getCard().getName() + ": " + targetName + "'s library is empty.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            return;
        }

        List<Card> topCards = takeTopCards(deck, actual);

        String logMsg = controllerName + " looks at the top " + pluralCards(actual) + " of " + targetName + "'s library.";
        gameBroadcastService.logAndBroadcast(gameData, logMsg);

        List<Card> sourceCards = new ArrayList<>(topCards);

        gameData.interaction.beginLibrarySearch(LibrarySearchParams.builder(controllerId, topCards)
                .canFailToFind(true)
                .targetPlayerId(targetPlayerId)
                .sourceCards(sourceCards)
                .reorderRemainingToTop(true)
                .shuffleAfterSelection(false)
                .prompt("You may exile one of these cards. The rest will be put on top of the library.")
                .destination(LibrarySearchDestination.EXILE)
                .build());

        List<CardView> cardViews = topCards.stream().map(cardViewFactory::create).toList();
        sessionManager.sendToPlayer(controllerId, new ChooseCardFromLibraryMessage(
                cardViews,
                "You may exile one of these cards. The rest will be put on top of the library.",
                true
        ));

        log.info("Game {} - {} looks at top {} of {}'s library ({})", gameData.id, controllerName, actual, targetName, entry.getCard().getName());
    }

    // =========================================================================
    // Helpers
    // =========================================================================

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

    private static List<Card> takeTopCards(List<Card> deck, int count) {
        List<Card> topCards = new ArrayList<>(deck.subList(0, count));
        deck.subList(0, count).clear();
        return topCards;
    }

    private static boolean matchesCardTypes(Card card, Set<CardType> cardTypes) {
        return cardTypes.contains(card.getType())
                || card.getAdditionalTypes().stream().anyMatch(cardTypes::contains);
    }

    private static String pluralCards(int count) {
        return count + " card" + (count != 1 ? "s" : "");
    }
}
