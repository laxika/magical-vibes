package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.InteractionContext;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
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
import com.github.laxika.magicalvibes.service.LegendRuleService;
import com.github.laxika.magicalvibes.service.PlayerInputService;
import com.github.laxika.magicalvibes.service.StateBasedActionService;
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
    private final LegendRuleService legendRuleService;
    private final StateBasedActionService stateBasedActionService;
    private final GameBroadcastService gameBroadcastService;
    private final CardViewFactory cardViewFactory;
    private final TurnProgressionService turnProgressionService;
    private final PlayerInputService playerInputService;

    public void handleLibraryCardsReordered(GameData gameData, Player player, List<Integer> cardOrder) {
        if (!gameData.interaction.isAwaitingInput(AwaitingInput.LIBRARY_REORDER)) {
            throw new IllegalStateException("Not awaiting library reorder");
        }
        InteractionContext.LibraryReorder libraryReorder = gameData.interaction.libraryReorderContext();
        if (libraryReorder == null || !player.getId().equals(libraryReorder.playerId())) {
            throw new IllegalStateException("Not your turn to reorder");
        }

        List<Card> reorderCards = libraryReorder.cards();
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

        if (libraryReorder.toBottom()) {
            for (int i = 0; i < count; i++) {
                deck.add(reorderCards.get(cardOrder.get(i)));
            }
        } else {
            for (int i = 0; i < count; i++) {
                deck.set(i, reorderCards.get(cardOrder.get(i)));
            }
        }

        // Clear awaiting state
        gameData.interaction.clearAwaitingInput();
        boolean reorderedToBottom = libraryReorder.toBottom();
        gameData.interaction.clearLibraryReorder();

        String logMsg = reorderedToBottom
                ? player.getUsername() + " puts " + count + " cards on the bottom of their library."
                : player.getUsername() + " puts " + count + " cards back on top of their library.";
        gameBroadcastService.logAndBroadcast(gameData, logMsg);
        log.info("Game {} - {} reordered {} {} cards", gameData.id, player.getUsername(), count,
                reorderedToBottom ? "bottom" : "top");

        if (reorderedToBottom && !gameData.pendingLibraryBottomReorders.isEmpty()) {
            gameHelper.beginNextPendingLibraryBottomReorder(gameData);
            return;
        }
        if (reorderedToBottom && gameData.warpWorldOperation.sourceName != null) {
            gameHelper.finalizePendingWarpWorld(gameData);
        }

        if (!gameData.interaction.isAwaitingInput() && !gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
            return;
        }

        turnProgressionService.resolveAutoPass(gameData);
    }

    public void handleHandTopBottomChosen(GameData gameData, Player player, int handCardIndex, int topCardIndex) {
        if (!gameData.interaction.isAwaitingInput(AwaitingInput.HAND_TOP_BOTTOM_CHOICE)) {
            throw new IllegalStateException("Not awaiting hand/top/bottom choice");
        }
        InteractionContext.HandTopBottomChoice handTopBottomChoice = gameData.interaction.handTopBottomChoiceContext();
        if (handTopBottomChoice == null || !player.getId().equals(handTopBottomChoice.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        List<Card> handTopBottomCards = handTopBottomChoice.cards();
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
        gameData.interaction.clearAwaitingInput();
        gameData.interaction.clearHandTopBottomChoice();

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
        if (!gameData.interaction.isAwaitingInput(AwaitingInput.LIBRARY_SEARCH)) {
            throw new IllegalStateException("Not awaiting library search");
        }
        InteractionContext.LibrarySearch librarySearch = gameData.interaction.librarySearchContext();
        if (librarySearch == null || !player.getId().equals(librarySearch.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        UUID playerId = player.getId();
        List<Card> searchCards = librarySearch.cards();

        boolean reveals = librarySearch.reveals();
        boolean canFailToFind = librarySearch.canFailToFind();
        UUID targetPlayerId = librarySearch.targetPlayerId();
        int remainingCount = librarySearch.remainingCount();
        List<Card> sourceCards = librarySearch.sourceCards();
        boolean reorderRemainingToBottom = librarySearch.reorderRemainingToBottom();
        boolean shuffleAfterSelection = librarySearch.shuffleAfterSelection();
        LibrarySearchDestination destination = librarySearch.destination() != null
                ? librarySearch.destination()
                : LibrarySearchDestination.HAND;

        UUID deckOwnerId = targetPlayerId != null ? targetPlayerId : playerId;
        UUID handOwnerId = targetPlayerId != null ? targetPlayerId : playerId;

        gameData.interaction.clearAwaitingInput();
        gameData.interaction.clearLibrarySearch();

        List<Card> deck = gameData.playerDecks.get(deckOwnerId);

        if (reorderRemainingToBottom) {
            if (sourceCards == null) {
                throw new IllegalStateException("Missing source cards for revealed-card choice");
            }

            Card chosenCard = null;
            if (cardIndex == -1) {
                if (!canFailToFind) {
                    throw new IllegalStateException("Cannot fail to find with an unrestricted search");
                }
            } else {
                if (cardIndex < 0 || cardIndex >= searchCards.size()) {
                    throw new IllegalStateException("Invalid card index: " + cardIndex);
                }
                chosenCard = searchCards.get(cardIndex);
                gameData.playerHands.get(handOwnerId).add(chosenCard);
                for (int i = 0; i < sourceCards.size(); i++) {
                    if (sourceCards.get(i).getId().equals(chosenCard.getId())) {
                        sourceCards.remove(i);
                        break;
                    }
                }
            }

            if (sourceCards.size() > 1) {
                gameData.interaction.beginLibraryReorder(deckOwnerId, sourceCards, true);
                List<CardView> cardViews = sourceCards.stream().map(cardViewFactory::create).toList();
                sessionManager.sendToPlayer(deckOwnerId, new com.github.laxika.magicalvibes.networking.message.ReorderLibraryCardsMessage(
                        cardViews,
                        "Put these cards on the bottom of your library in any order (first chosen will be closest to the top)."
                ));
            } else if (sourceCards.size() == 1) {
                deck.add(sourceCards.getFirst());
            }

            String logEntry = chosenCard == null
                    ? player.getUsername() + " does not reveal a creature card."
                    : player.getUsername() + " reveals " + chosenCard.getName() + " and puts it into their hand.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);

            if (sourceCards.size() > 1) {
                return;
            }

            turnProgressionService.resolveAutoPass(gameData);
            return;
        }

        if (cardIndex == -1) {
            if (!canFailToFind) {
                throw new IllegalStateException("Cannot fail to find with an unrestricted search");
            }
            if (shuffleAfterSelection) {
                Collections.shuffle(deck);
            }
            String logEntry = shuffleAfterSelection
                    ? player.getUsername() + " chooses not to take a card. Library is shuffled."
                    : player.getUsername() + " chooses not to take a card.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} declines to take a card from library", gameData.id, player.getUsername());
            turnProgressionService.resolveAutoPass(gameData);
            return;
        }

        if (cardIndex < 0 || cardIndex >= searchCards.size()) {
            throw new IllegalStateException("Invalid card index: " + cardIndex);
        }

        Card chosenCard = searchCards.get(cardIndex);

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

        if (destination == LibrarySearchDestination.HAND) {
            gameData.playerHands.get(handOwnerId).add(chosenCard);
        } else {
            List<Permanent> battlefield = gameData.playerBattlefields.get(handOwnerId);
            Permanent perm = new Permanent(chosenCard);
            battlefield.add(perm);

            String battlefieldOwner = gameData.playerIdToName.get(handOwnerId);
            String entersLog = chosenCard.getName() + " enters the battlefield under " + battlefieldOwner + "'s control.";
            gameBroadcastService.logAndBroadcast(gameData, entersLog);

            if (chosenCard.getType() == CardType.CREATURE) {
                gameHelper.handleCreatureEnteredBattlefield(gameData, handOwnerId, chosenCard, null, false);
            }
            if (chosenCard.getType() == CardType.PLANESWALKER && chosenCard.getLoyalty() != null) {
                perm.setLoyaltyCounters(chosenCard.getLoyalty());
                perm.setSummoningSick(false);
            }
            if (!gameData.interaction.isAwaitingInput()) {
                legendRuleService.checkLegendRule(gameData, handOwnerId);
            }
        }

        if (targetPlayerId != null && remainingCount > 1) {
            int newRemaining = remainingCount - 1;
            List<Card> newSearchCards = new ArrayList<>(deck);

            gameData.interaction.beginLibrarySearch(playerId, newSearchCards, false, false, targetPlayerId, newRemaining);

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

        if (shuffleAfterSelection) {
            Collections.shuffle(deck);
        }

        String destinationText = destination == LibrarySearchDestination.BATTLEFIELD
                ? "onto the battlefield"
                : "into their hand";
        String logEntry;
        if (targetPlayerId != null) {
            String targetName = gameData.playerIdToName.get(targetPlayerId);
            logEntry = shuffleAfterSelection
                    ? player.getUsername() + " puts cards " + destinationText + " for " + targetName + ". " + targetName + "'s library is shuffled."
                    : player.getUsername() + " puts cards " + destinationText + " for " + targetName + ".";
        } else if (reveals) {
            logEntry = shuffleAfterSelection
                    ? player.getUsername() + " reveals " + chosenCard.getName() + " and puts it " + destinationText + ". Library is shuffled."
                    : player.getUsername() + " reveals " + chosenCard.getName() + " and puts it " + destinationText + ".";
        } else {
            logEntry = shuffleAfterSelection
                    ? player.getUsername() + " puts a card " + destinationText + ". Library is shuffled."
                    : player.getUsername() + " puts a card " + destinationText + ".";
        }
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} searches library and puts {} {}",
                gameData.id, player.getUsername(), chosenCard.getName(), destinationText);

        if (destination == LibrarySearchDestination.BATTLEFIELD) {
            stateBasedActionService.performStateBasedActions(gameData);
        }

        turnProgressionService.resolveAutoPass(gameData);
    }
    public void handleLibraryRevealChoice(GameData gameData, Player player, List<UUID> cardIds) {
        InteractionContext.LibraryRevealChoice libraryRevealChoice = gameData.interaction.libraryRevealChoiceContext();
        if (libraryRevealChoice == null || !player.getId().equals(libraryRevealChoice.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        Set<UUID> validIds = libraryRevealChoice.validCardIds();
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

        UUID controllerId = libraryRevealChoice.playerId();
        List<Card> allRevealedCards = libraryRevealChoice.allCards();
        String playerName = gameData.playerIdToName.get(controllerId);

        // Clear awaiting state
        gameData.interaction.clearAwaitingInput();
        gameData.interaction.clearLibraryRevealChoice();

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
                gameHelper.handleCreatureEnteredBattlefield(gameData, controllerId, card, null, false);
            }
            if (card.getType() == CardType.PLANESWALKER && card.getLoyalty() != null) {
                perm.setLoyaltyCounters(card.getLoyalty());
                perm.setSummoningSick(false);
            }
            if (!gameData.interaction.isAwaitingInput()) {
                legendRuleService.checkLegendRule(gameData, controllerId);
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

        stateBasedActionService.performStateBasedActions(gameData);
        turnProgressionService.resolveAutoPass(gameData);
    }
}



