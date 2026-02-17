package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.ChooseCardFromLibraryMessage;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameHelper;
import com.github.laxika.magicalvibes.service.GameQueryService;
import com.github.laxika.magicalvibes.service.TurnProgressionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LibraryChoiceHandlerService {

    private final SessionManager sessionManager;
    private final GameQueryService gameQueryService;
    private final GameHelper gameHelper;
    private final GameBroadcastService gameBroadcastService;
    private final CardViewFactory cardViewFactory;
    private final TurnProgressionService turnProgressionService;

    public void handleLibraryCardsReordered(GameData gameData, Player player, List<Integer> cardOrder) {
        if (gameData.awaitingInput != AwaitingInput.LIBRARY_REORDER) {
            throw new IllegalStateException("Not awaiting library reorder");
        }
        if (!player.getId().equals(gameData.awaitingLibraryReorderPlayerId)) {
            throw new IllegalStateException("Not your turn to reorder");
        }

        List<Card> reorderCards = gameData.awaitingLibraryReorderCards;
        int count = reorderCards.size();

        if (cardOrder.size() != count) {
            throw new IllegalStateException("Must specify order for all " + count + " cards");
        }

        // Validate that cardOrder is a permutation of 0..count-1
        Set<Integer> seen = new HashSet<>();
        for (int idx : cardOrder) {
            if (idx < 0 || idx >= count) {
                throw new IllegalStateException("Invalid card index: " + idx);
            }
            if (!seen.add(idx)) {
                throw new IllegalStateException("Duplicate card index: " + idx);
            }
        }

        // Apply the reorder: replace top N cards of deck with the reordered ones
        List<Card> deck = gameData.playerDecks.get(player.getId());
        for (int i = 0; i < count; i++) {
            deck.set(i, reorderCards.get(cardOrder.get(i)));
        }

        // Clear awaiting state
        gameData.awaitingInput = null;
        gameData.awaitingLibraryReorderPlayerId = null;
        gameData.awaitingLibraryReorderCards = null;

        String logMsg = player.getUsername() + " puts " + count + " cards back on top of their library.";
        gameBroadcastService.logAndBroadcast(gameData, logMsg);
        log.info("Game {} - {} reordered top {} cards", gameData.id, player.getUsername(), count);

        turnProgressionService.resolveAutoPass(gameData);
    }

    public void handleHandTopBottomChosen(GameData gameData, Player player, int handCardIndex, int topCardIndex) {
        if (gameData.awaitingInput != AwaitingInput.HAND_TOP_BOTTOM_CHOICE) {
            throw new IllegalStateException("Not awaiting hand/top/bottom choice");
        }
        if (!player.getId().equals(gameData.awaitingHandTopBottomPlayerId)) {
            throw new IllegalStateException("Not your turn to choose");
        }

        List<Card> handTopBottomCards = gameData.awaitingHandTopBottomCards;
        int count = handTopBottomCards.size();

        if (handCardIndex < 0 || handCardIndex >= count) {
            throw new IllegalStateException("Invalid hand card index: " + handCardIndex);
        }
        if (topCardIndex < 0 || topCardIndex >= count) {
            throw new IllegalStateException("Invalid top card index: " + topCardIndex);
        }
        if (handCardIndex == topCardIndex) {
            throw new IllegalStateException("Hand and top card indices must be different");
        }

        UUID playerId = player.getId();
        List<Card> deck = gameData.playerDecks.get(playerId);

        // Put the chosen card into hand
        Card handCard = handTopBottomCards.get(handCardIndex);
        gameData.playerHands.get(playerId).add(handCard);

        // Put the chosen card on top of library
        Card topCard = handTopBottomCards.get(topCardIndex);
        deck.add(0, topCard);

        // Put the remaining card on the bottom of library
        for (int i = 0; i < count; i++) {
            if (i != handCardIndex && i != topCardIndex) {
                deck.add(handTopBottomCards.get(i));
            }
        }

        // Clear awaiting state
        gameData.awaitingInput = null;
        gameData.awaitingHandTopBottomPlayerId = null;
        gameData.awaitingHandTopBottomCards = null;

        String logMsg;
        if (count == 2) {
            logMsg = player.getUsername() + " puts one card into their hand and one on top of their library.";
        } else {
            logMsg = player.getUsername() + " puts one card into their hand, one on top of their library, and one on the bottom.";
        }
        gameBroadcastService.logAndBroadcast(gameData, logMsg);
        log.info("Game {} - {} completed hand/top/bottom choice", gameData.id, player.getUsername());

        turnProgressionService.resolveAutoPass(gameData);
    }

    public void handleLibraryCardChosen(GameData gameData, Player player, int cardIndex) {
        if (gameData.awaitingInput != AwaitingInput.LIBRARY_SEARCH) {
            throw new IllegalStateException("Not awaiting library search");
        }
        if (!player.getId().equals(gameData.awaitingLibrarySearchPlayerId)) {
            throw new IllegalStateException("Not your turn to choose");
        }

        UUID playerId = player.getId();
        List<Card> searchCards = gameData.awaitingLibrarySearchCards;

        boolean reveals = gameData.awaitingLibrarySearchReveals;
        boolean canFailToFind = gameData.awaitingLibrarySearchCanFailToFind;
        UUID targetPlayerId = gameData.awaitingLibrarySearchTargetPlayerId;
        int remainingCount = gameData.awaitingLibrarySearchRemainingCount;

        // Determine whose library/hand to use
        UUID deckOwnerId = targetPlayerId != null ? targetPlayerId : playerId;
        UUID handOwnerId = targetPlayerId != null ? targetPlayerId : playerId;

        // Clear all state
        gameData.awaitingInput = null;
        gameData.awaitingLibrarySearchPlayerId = null;
        gameData.awaitingLibrarySearchCards = null;
        gameData.awaitingLibrarySearchReveals = false;
        gameData.awaitingLibrarySearchCanFailToFind = false;
        gameData.awaitingLibrarySearchTargetPlayerId = null;
        gameData.awaitingLibrarySearchRemainingCount = 0;

        List<Card> deck = gameData.playerDecks.get(deckOwnerId);

        if (cardIndex == -1) {
            // Player declined (fail to find) — only allowed for restricted searches (e.g. basic land)
            if (!canFailToFind) {
                throw new IllegalStateException("Cannot fail to find with an unrestricted search");
            }
            Collections.shuffle(deck);
            String logEntry = player.getUsername() + " chooses not to take a card. Library is shuffled.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} declines to take a card from library", gameData.id, player.getUsername());
        } else {
            if (cardIndex < 0 || cardIndex >= searchCards.size()) {
                throw new IllegalStateException("Invalid card index: " + cardIndex);
            }

            Card chosenCard = searchCards.get(cardIndex);

            // Remove the chosen card from the library by identity
            boolean removed = false;
            for (int i = 0; i < deck.size(); i++) {
                if (deck.get(i).getId().equals(chosenCard.getId())) {
                    deck.remove(i);
                    removed = true;
                    break;
                }
            }

            if (!removed) {
                throw new IllegalStateException("Chosen card not found in library");
            }

            gameData.playerHands.get(handOwnerId).add(chosenCard);

            // Head Games multi-pick: more cards to choose
            if (targetPlayerId != null && remainingCount > 1) {
                int newRemaining = remainingCount - 1;
                List<Card> newSearchCards = new ArrayList<>(deck);

                gameData.awaitingInput = AwaitingInput.LIBRARY_SEARCH;
                gameData.awaitingLibrarySearchPlayerId = playerId;
                gameData.awaitingLibrarySearchCards = newSearchCards;
                gameData.awaitingLibrarySearchReveals = false;
                gameData.awaitingLibrarySearchCanFailToFind = false;
                gameData.awaitingLibrarySearchTargetPlayerId = targetPlayerId;
                gameData.awaitingLibrarySearchRemainingCount = newRemaining;

                String targetName = gameData.playerIdToName.get(targetPlayerId);
                List<CardView> cardViews = newSearchCards.stream().map(cardViewFactory::create).toList();
                sessionManager.sendToPlayer(playerId, new ChooseCardFromLibraryMessage(
                        cardViews,
                        "Search " + targetName + "'s library for a card to put into their hand (" + newRemaining + " remaining).",
                        false
                ));

                log.info("Game {} - {} picks for Head Games, {} remaining", gameData.id, player.getUsername(), newRemaining);
                return;
            }

            Collections.shuffle(deck);

            String logEntry;
            if (targetPlayerId != null) {
                String targetName = gameData.playerIdToName.get(targetPlayerId);
                logEntry = player.getUsername() + " puts cards into " + targetName + "'s hand. " + targetName + "'s library is shuffled.";
            } else if (reveals) {
                logEntry = player.getUsername() + " reveals " + chosenCard.getName() + " and puts it into their hand. Library is shuffled.";
            } else {
                logEntry = player.getUsername() + " puts a card into their hand. Library is shuffled.";
            }
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} searches library and puts {} into hand", gameData.id, player.getUsername(), chosenCard.getName());
        }

        turnProgressionService.resolveAutoPass(gameData);
    }

    public void handleLibraryRevealChoice(GameData gameData, Player player, List<UUID> cardIds) {
        if (!player.getId().equals(gameData.awaitingLibraryRevealPlayerId)) {
            throw new IllegalStateException("Not your turn to choose");
        }

        Set<UUID> validIds = gameData.awaitingLibraryRevealValidCardIds;
        if (cardIds == null) {
            cardIds = List.of();
        }

        for (UUID cardId : cardIds) {
            if (!validIds.contains(cardId)) {
                throw new IllegalStateException("Invalid card: " + cardId);
            }
        }

        Set<UUID> uniqueIds = new HashSet<>(cardIds);
        if (uniqueIds.size() != cardIds.size()) {
            throw new IllegalStateException("Duplicate card IDs in selection");
        }

        UUID controllerId = gameData.awaitingLibraryRevealPlayerId;
        List<Card> allRevealedCards = gameData.awaitingLibraryRevealAllCards;
        String playerName = gameData.playerIdToName.get(controllerId);

        // Clear awaiting state
        gameData.awaitingInput = null;
        gameData.awaitingLibraryRevealPlayerId = null;
        gameData.awaitingLibraryRevealValidCardIds = null;
        gameData.awaitingLibraryRevealAllCards = null;

        // Separate selected cards from the rest
        Set<UUID> selectedIds = new HashSet<>(cardIds);
        List<Card> selectedCards = new ArrayList<>();
        List<Card> remainingCards = new ArrayList<>();
        for (Card card : allRevealedCards) {
            if (selectedIds.contains(card.getId())) {
                selectedCards.add(card);
            } else {
                remainingCards.add(card);
            }
        }

        // Put selected cards onto the battlefield
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        for (Card card : selectedCards) {
            Permanent perm = new Permanent(card);
            battlefield.add(perm);

            String logEntry = card.getName() + " enters the battlefield under " + playerName + "'s control.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);

            if (card.getType() == CardType.CREATURE) {
                gameHelper.handleCreatureEnteredBattlefield(gameData, controllerId, card, null);
            }
            if (card.getType() == CardType.PLANESWALKER && card.getLoyalty() != null) {
                perm.setLoyaltyCounters(card.getLoyalty());
                perm.setSummoningSick(false);
            }
            if (gameData.awaitingInput == null) {
                gameHelper.checkLegendRule(gameData, controllerId);
            }
        }

        // Shuffle remaining cards back into library
        List<Card> deck = gameData.playerDecks.get(controllerId);
        deck.addAll(remainingCards);
        Collections.shuffle(deck);

        if (selectedCards.isEmpty()) {
            String logEntry = playerName + " puts no cards onto the battlefield. Library is shuffled.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
        } else {
            String names = selectedCards.stream().map(Card::getName).reduce((a, b) -> a + ", " + b).orElse("");
            String logEntry = playerName + " puts " + names + " onto the battlefield. Library is shuffled.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
        }

        log.info("Game {} - {} resolves library reveal choice, {} cards to battlefield", gameData.id, playerName, selectedCards.size());

        gameHelper.performStateBasedActions(gameData);
        turnProgressionService.resolveAutoPass(gameData);
    }
}
