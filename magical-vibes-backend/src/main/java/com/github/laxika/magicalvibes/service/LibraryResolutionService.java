package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.service.effect.EffectHandlerProvider;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.AjaniUltimateEffect;
import com.github.laxika.magicalvibes.model.effect.HeadGamesEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsHandTopBottomEffect;
import com.github.laxika.magicalvibes.model.effect.MillByHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.MillHalfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.ReorderTopCardsOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForBasicLandToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardToHandEffect;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class LibraryResolutionService implements EffectHandlerProvider {

    private final GameBroadcastService gameBroadcastService;
    private final SessionManager sessionManager;
    private final CardViewFactory cardViewFactory;

    @Override
    public void registerHandlers(EffectHandlerRegistry registry) {
        registry.register(ShuffleIntoLibraryEffect.class,
                (gd, entry, effect) -> resolveShuffleIntoLibrary(gd, entry));
        registry.register(ShuffleGraveyardIntoLibraryEffect.class,
                (gd, entry, effect) -> resolveShuffleGraveyardIntoLibrary(gd, entry));
        registry.register(MillByHandSizeEffect.class,
                (gd, entry, effect) -> resolveMillByHandSize(gd, entry));
        registry.register(MillTargetPlayerEffect.class,
                (gd, entry, effect) -> resolveMillTargetPlayer(gd, entry, (MillTargetPlayerEffect) effect));
        registry.register(MillHalfLibraryEffect.class,
                (gd, entry, effect) -> resolveMillHalfLibrary(gd, entry));
        registry.register(RevealTopCardOfLibraryEffect.class,
                (gd, entry, effect) -> resolveRevealTopCardOfLibrary(gd, entry));
        registry.register(ReorderTopCardsOfLibraryEffect.class,
                (gd, entry, effect) -> resolveReorderTopCardsOfLibrary(gd, entry, (ReorderTopCardsOfLibraryEffect) effect));
        registry.register(SearchLibraryForBasicLandToHandEffect.class,
                (gd, entry, effect) -> resolveSearchLibraryForBasicLandToHand(gd, entry));
        registry.register(SearchLibraryForCardToHandEffect.class,
                (gd, entry, effect) -> resolveSearchLibraryForCardToHand(gd, entry));
        registry.register(SearchLibraryForCreatureWithMVXOrLessToHandEffect.class,
                (gd, entry, effect) -> resolveSearchLibraryForCreatureWithMVXOrLessToHand(gd, entry));
        registry.register(LookAtTopCardsHandTopBottomEffect.class,
                (gd, entry, effect) -> resolveLookAtTopCardsHandTopBottom(gd, entry, (LookAtTopCardsHandTopBottomEffect) effect));
        registry.register(HeadGamesEffect.class,
                (gd, entry, effect) -> resolveHeadGames(gd, entry));
        registry.register(AjaniUltimateEffect.class,
                (gd, entry, effect) -> resolveAjaniUltimate(gd, entry));
    }

    void resolveShuffleIntoLibrary(GameData gameData, StackEntry entry) {
        List<Card> deck = gameData.playerDecks.get(entry.getControllerId());
        deck.add(entry.getCard());
        Collections.shuffle(deck);

        String shuffleLog = entry.getCard().getName() + " is shuffled into its owner's library.";
        gameBroadcastService.logAndBroadcast(gameData, shuffleLog);
    }

    void resolveMillByHandSize(GameData gameData, StackEntry entry) {
        UUID targetPlayerId = entry.getTargetPermanentId();
        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        int handSize = hand != null ? hand.size() : 0;
        String playerName = gameData.playerIdToName.get(targetPlayerId);

        if (handSize == 0) {
            String logEntry = playerName + " has no cards in hand — mills nothing.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        List<Card> graveyard = gameData.playerGraveyards.get(targetPlayerId);

        int cardsToMill = Math.min(handSize, deck.size());
        for (int i = 0; i < cardsToMill; i++) {
            Card card = deck.removeFirst();
            graveyard.add(card);
        }

        String logEntry = playerName + " mills " + cardsToMill + " card" + (cardsToMill != 1 ? "s" : "") + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} mills {} cards (hand size)", gameData.id, playerName, cardsToMill);
    }

    void resolveMillTargetPlayer(GameData gameData, StackEntry entry, MillTargetPlayerEffect mill) {
        UUID targetPlayerId = entry.getTargetPermanentId();
        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        List<Card> graveyard = gameData.playerGraveyards.get(targetPlayerId);
        String playerName = gameData.playerIdToName.get(targetPlayerId);

        int cardsToMill = Math.min(mill.count(), deck.size());
        for (int i = 0; i < cardsToMill; i++) {
            Card card = deck.removeFirst();
            graveyard.add(card);
        }

        String logEntry = playerName + " mills " + cardsToMill + " card" + (cardsToMill != 1 ? "s" : "") + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} mills {} cards", gameData.id, playerName, cardsToMill);
    }

    void resolveMillHalfLibrary(GameData gameData, StackEntry entry) {
        UUID targetPlayerId = entry.getTargetPermanentId();
        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        List<Card> graveyard = gameData.playerGraveyards.get(targetPlayerId);
        String playerName = gameData.playerIdToName.get(targetPlayerId);

        int cardsToMill = deck.size() / 2;
        if (cardsToMill == 0) {
            String logEntry = playerName + "'s library has " + deck.size() + " card" + (deck.size() != 1 ? "s" : "") + " — mills nothing.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        for (int i = 0; i < cardsToMill; i++) {
            Card card = deck.removeFirst();
            graveyard.add(card);
        }

        String logEntry = playerName + " mills half their library (" + cardsToMill + " card" + (cardsToMill != 1 ? "s" : "") + ").";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} mills half library ({} cards)", gameData.id, playerName, cardsToMill);
    }

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

        String logEntry = playerName + " shuffles their graveyard (" + count + " card" + (count != 1 ? "s" : "") + ") into their library.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} shuffles graveyard ({} cards) into library", gameData.id, playerName, count);
    }

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

        gameData.awaitingLibraryReorderPlayerId = controllerId;
        gameData.awaitingLibraryReorderCards = topCards;
        gameData.awaitingInput = AwaitingInput.LIBRARY_REORDER;

        List<CardView> cardViews = topCards.stream().map(cardViewFactory::create).toList();
        sessionManager.sendToPlayer(controllerId, new ReorderLibraryCardsMessage(
                cardViews,
                "Put these cards back on top of your library in any order (top to bottom)."
        ));

        String logMsg = gameData.playerIdToName.get(controllerId) + " looks at the top " + count + " cards of their library.";
        gameBroadcastService.logAndBroadcast(gameData, logMsg);
        log.info("Game {} - {} reordering top {} cards of library", gameData.id, gameData.playerIdToName.get(controllerId), count);
    }

    void resolveSearchLibraryForBasicLandToHand(GameData gameData, StackEntry entry) {
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);

        if (deck == null || deck.isEmpty()) {
            String logMsg = playerName + " searches their library but it is empty. Library is shuffled.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            return;
        }

        List<Card> basicLands = new ArrayList<>();
        for (Card card : deck) {
            if (card.getType() == CardType.BASIC_LAND) {
                basicLands.add(card);
            }
        }

        if (basicLands.isEmpty()) {
            Collections.shuffle(deck);
            String logMsg = playerName + " searches their library but finds no basic land cards. Library is shuffled.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            log.info("Game {} - {} searches library, no basic lands found", gameData.id, playerName);
            return;
        }

        gameData.awaitingLibrarySearchPlayerId = controllerId;
        gameData.awaitingLibrarySearchCards = basicLands;
        gameData.awaitingLibrarySearchReveals = true;
        gameData.awaitingLibrarySearchCanFailToFind = true;
        gameData.awaitingInput = AwaitingInput.LIBRARY_SEARCH;

        List<CardView> cardViews = basicLands.stream().map(cardViewFactory::create).toList();
        sessionManager.sendToPlayer(controllerId, new ChooseCardFromLibraryMessage(
                cardViews,
                "Search your library for a basic land card to put into your hand.",
                true
        ));

        String logMsg = playerName + " searches their library.";
        gameBroadcastService.logAndBroadcast(gameData, logMsg);
        log.info("Game {} - {} searching library for a basic land ({} found)", gameData.id, playerName, basicLands.size());
    }

    void resolveSearchLibraryForCardToHand(GameData gameData, StackEntry entry) {
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);

        if (deck == null || deck.isEmpty()) {
            String logMsg = playerName + " searches their library but it is empty. Library is shuffled.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            return;
        }

        List<Card> allCards = new ArrayList<>(deck);

        gameData.awaitingLibrarySearchPlayerId = controllerId;
        gameData.awaitingLibrarySearchCards = allCards;
        gameData.awaitingLibrarySearchReveals = false;
        gameData.awaitingLibrarySearchCanFailToFind = false;
        gameData.awaitingInput = AwaitingInput.LIBRARY_SEARCH;

        List<CardView> cardViews = allCards.stream().map(cardViewFactory::create).toList();
        sessionManager.sendToPlayer(controllerId, new ChooseCardFromLibraryMessage(
                cardViews,
                "Search your library for a card to put into your hand.",
                false
        ));

        String logMsg = playerName + " searches their library.";
        gameBroadcastService.logAndBroadcast(gameData, logMsg);
        log.info("Game {} - {} searching library for any card ({} cards in library)", gameData.id, playerName, allCards.size());
    }

    void resolveSearchLibraryForCreatureWithMVXOrLessToHand(GameData gameData, StackEntry entry) {
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        int maxMV = entry.getXValue();

        if (deck == null || deck.isEmpty()) {
            String logMsg = playerName + " searches their library but it is empty. Library is shuffled.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            return;
        }

        List<Card> eligibleCreatures = new ArrayList<>();
        for (Card card : deck) {
            if (card.getType() == CardType.CREATURE && card.getManaValue() <= maxMV) {
                eligibleCreatures.add(card);
            }
        }

        if (eligibleCreatures.isEmpty()) {
            Collections.shuffle(deck);
            String logMsg = playerName + " searches their library but finds no creature card with mana value " + maxMV + " or less. Library is shuffled.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            log.info("Game {} - {} searches library, no eligible creatures found (MV <= {})", gameData.id, playerName, maxMV);
            return;
        }

        gameData.awaitingLibrarySearchPlayerId = controllerId;
        gameData.awaitingLibrarySearchCards = eligibleCreatures;
        gameData.awaitingLibrarySearchReveals = true;
        gameData.awaitingLibrarySearchCanFailToFind = true;
        gameData.awaitingInput = AwaitingInput.LIBRARY_SEARCH;

        List<CardView> cardViews = eligibleCreatures.stream().map(cardViewFactory::create).toList();
        sessionManager.sendToPlayer(controllerId, new ChooseCardFromLibraryMessage(
                cardViews,
                "Search your library for a creature card with mana value " + maxMV + " or less to reveal and put into your hand.",
                true
        ));

        String logMsg = playerName + " searches their library.";
        gameBroadcastService.logAndBroadcast(gameData, logMsg);
        log.info("Game {} - {} searching library for creature with MV <= {} ({} found)", gameData.id, playerName, maxMV, eligibleCreatures.size());
    }

    void resolveHeadGames(GameData gameData, StackEntry entry) {
        UUID casterId = entry.getControllerId();
        UUID targetPlayerId = entry.getTargetPermanentId();
        List<Card> targetHand = gameData.playerHands.get(targetPlayerId);
        List<Card> targetDeck = gameData.playerDecks.get(targetPlayerId);
        String casterName = gameData.playerIdToName.get(casterId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);

        int handSize = targetHand.size();

        // Step 1: Target opponent puts hand on top of library
        if (handSize == 0) {
            String logMsg = targetName + " has no cards in hand. " + targetName + "'s library is shuffled.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            Collections.shuffle(targetDeck);
            return;
        }

        // Put hand cards on top of library (in order)
        for (int i = targetHand.size() - 1; i >= 0; i--) {
            targetDeck.addFirst(targetHand.get(i));
        }
        targetHand.clear();

        String logMsg = targetName + " puts " + handSize + " card" + (handSize != 1 ? "s" : "")
                + " from their hand on top of their library.";
        gameBroadcastService.logAndBroadcast(gameData, logMsg);

        // Step 2: Caster searches target's library for that many cards
        List<Card> allCards = new ArrayList<>(targetDeck);

        gameData.awaitingLibrarySearchPlayerId = casterId;
        gameData.awaitingLibrarySearchCards = allCards;
        gameData.awaitingLibrarySearchReveals = false;
        gameData.awaitingLibrarySearchCanFailToFind = false;
        gameData.awaitingLibrarySearchTargetPlayerId = targetPlayerId;
        gameData.awaitingLibrarySearchRemainingCount = handSize;
        gameData.awaitingInput = AwaitingInput.LIBRARY_SEARCH;

        List<CardView> cardViews = allCards.stream().map(cardViewFactory::create).toList();
        sessionManager.sendToPlayer(casterId, new ChooseCardFromLibraryMessage(
                cardViews,
                "Search " + targetName + "'s library for a card to put into their hand (" + handSize + " remaining).",
                false
        ));

        String searchLog = casterName + " searches " + targetName + "'s library.";
        gameBroadcastService.logAndBroadcast(gameData, searchLog);
        log.info("Game {} - {} resolving Head Games, searching {}'s library for {} cards",
                gameData.id, casterName, targetName, handSize);
    }

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
        List<Card> revealedCards = new ArrayList<>(deck.subList(0, count));
        deck.subList(0, count).clear();

        // Filter to nonland permanent cards with MV ≤ 3
        List<Card> eligibleCards = new ArrayList<>();
        for (Card card : revealedCards) {
            if (card.getType() != null
                    && card.getType() != CardType.BASIC_LAND
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

        gameData.awaitingLibraryRevealPlayerId = controllerId;
        gameData.awaitingLibraryRevealAllCards = revealedCards;
        gameData.awaitingLibraryRevealValidCardIds = validCardIds;
        gameData.awaitingInput = AwaitingInput.LIBRARY_REVEAL_CHOICE;

        List<CardView> cardViews = eligibleCards.stream().map(cardViewFactory::create).toList();
        List<UUID> cardIds = eligibleCards.stream().map(Card::getId).toList();
        sessionManager.sendToPlayer(controllerId, new ChooseMultipleCardsFromGraveyardsMessage(
                cardIds, cardViews, eligibleCards.size(),
                "Choose any number of nonland permanent cards with mana value 3 or less to put onto the battlefield."
        ));

        log.info("Game {} - {} resolving Ajani ultimate with {} revealed, {} eligible", gameData.id, playerName, count, eligibleCards.size());
    }

    void resolveLookAtTopCardsHandTopBottom(GameData gameData, StackEntry entry, LookAtTopCardsHandTopBottomEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);

        int count = Math.min(effect.count(), deck.size());
        if (count == 0) {
            String logMsg = entry.getCard().getName() + ": " + playerName + "'s library is empty, nothing to look at.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            return;
        }

        if (count == 1) {
            // Only 1 card: it goes to hand
            Card card = deck.remove(0);
            gameData.playerHands.get(controllerId).add(card);
            String logMsg = playerName + " looks at the top card of their library and puts it into their hand.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            return;
        }

        List<Card> topCards = new ArrayList<>(deck.subList(0, count));
        // Remove the top cards from the deck temporarily
        deck.subList(0, count).clear();

        gameData.awaitingHandTopBottomPlayerId = controllerId;
        gameData.awaitingHandTopBottomCards = topCards;
        gameData.awaitingInput = AwaitingInput.HAND_TOP_BOTTOM_CHOICE;

        List<CardView> cardViews = topCards.stream().map(cardViewFactory::create).toList();
        sessionManager.sendToPlayer(controllerId, new ChooseHandTopBottomMessage(
                cardViews,
                "Look at the top " + count + " cards of your library. Choose one to put into your hand."
        ));

        String logMsg = playerName + " looks at the top " + count + " cards of their library.";
        gameBroadcastService.logAndBroadcast(gameData, logMsg);
        log.info("Game {} - {} resolving {} with {} cards", gameData.id, playerName, entry.getCard().getName(), count);
    }
}
