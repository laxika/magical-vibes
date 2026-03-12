package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.InteractionContext;
import com.github.laxika.magicalvibes.model.LibraryBottomReorderRequest;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.PendingOpponentExileChoice;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.effect.OpponentMayReturnExiledCardOrDrawEffect;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.ChooseCardFromLibraryMessage;
import com.github.laxika.magicalvibes.networking.message.ReorderLibraryCardsMessage;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import com.github.laxika.magicalvibes.service.WarpWorldService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.LegendRuleService;
import com.github.laxika.magicalvibes.service.PlayerInputService;
import com.github.laxika.magicalvibes.service.StateBasedActionService;
import com.github.laxika.magicalvibes.service.TurnProgressionService;
import com.github.laxika.magicalvibes.service.effect.EffectResolutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.EnumSet;
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
    private final GraveyardService graveyardService;
    private final WarpWorldService warpWorldService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final LegendRuleService legendRuleService;
    private final StateBasedActionService stateBasedActionService;
    private final GameBroadcastService gameBroadcastService;
    private final CardViewFactory cardViewFactory;
    private final TurnProgressionService turnProgressionService;
    private final PlayerInputService playerInputService;
    private final EffectResolutionService effectResolutionService;

    public void handleScryCompleted(GameData gameData, Player player, List<Integer> topCardOrder, List<Integer> bottomCardOrder) {
        if (!gameData.interaction.isAwaitingInput(AwaitingInput.SCRY)) {
            throw new IllegalStateException("Not awaiting scry");
        }
        InteractionContext.Scry scryContext = gameData.interaction.scryContext();
        if (scryContext == null || !player.getId().equals(scryContext.playerId())) {
            throw new IllegalStateException("Not your turn to scry");
        }

        List<Card> scryCards = scryContext.cards();
        int count = scryCards.size();

        if (topCardOrder.size() + bottomCardOrder.size() != count) {
            throw new IllegalStateException("Must assign all " + count + " cards");
        }

        // Validate indices are a valid permutation of 0..count-1
        Set<Integer> seen = new HashSet<>();
        for (int idx : topCardOrder) {
            if (idx < 0 || idx >= count) {
                throw new IllegalStateException("Invalid card index: " + idx);
            }
            if (!seen.add(idx)) {
                throw new IllegalStateException("Duplicate card index: " + idx);
            }
        }
        for (int idx : bottomCardOrder) {
            if (idx < 0 || idx >= count) {
                throw new IllegalStateException("Invalid card index: " + idx);
            }
            if (!seen.add(idx)) {
                throw new IllegalStateException("Duplicate card index: " + idx);
            }
        }

        List<Card> deck = gameData.playerDecks.get(player.getId());

        // Put top cards on top of library in order (first in list = top of library)
        for (int i = topCardOrder.size() - 1; i >= 0; i--) {
            deck.add(0, scryCards.get(topCardOrder.get(i)));
        }

        // Put bottom cards on bottom of library in order
        for (int idx : bottomCardOrder) {
            deck.add(scryCards.get(idx));
        }

        // Clear awaiting state
        gameData.interaction.clearAwaitingInput();
        gameData.interaction.clearScry();

        String logMsg;
        if (bottomCardOrder.isEmpty()) {
            logMsg = player.getUsername() + " puts " + count + " card(s) on top of their library.";
        } else if (topCardOrder.isEmpty()) {
            logMsg = player.getUsername() + " puts " + count + " card(s) on the bottom of their library.";
        } else {
            logMsg = player.getUsername() + " puts " + topCardOrder.size() + " card(s) on top and "
                    + bottomCardOrder.size() + " on the bottom of their library.";
        }
        gameBroadcastService.logAndBroadcast(gameData, logMsg);
        log.info("Game {} - {} scry completed: {} top, {} bottom", gameData.id, player.getUsername(),
                topCardOrder.size(), bottomCardOrder.size());

        if (!gameData.interaction.isAwaitingInput() && !gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
            return;
        }

        // Resume resolving remaining effects on the same spell/ability
        // (e.g. Foresee: "Scry 4, then draw two cards.")
        if (gameData.pendingEffectResolutionEntry != null) {
            effectResolutionService.resolveEffectsFrom(gameData,
                    gameData.pendingEffectResolutionEntry,
                    gameData.pendingEffectResolutionIndex);
        }

        turnProgressionService.resolveAutoPass(gameData);
    }

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
        UUID reorderDeckOwnerId = libraryReorder.deckOwnerId() != null ? libraryReorder.deckOwnerId() : player.getId();
        List<Card> deck = gameData.playerDecks.get(reorderDeckOwnerId);

        if (libraryReorder.toBottom()) {
            for (int i = 0; i < count; i++) {
                deck.add(reorderCards.get(cardOrder.get(i)));
            }
        } else {
            for (int i = 0; i < count; i++) {
                deck.add(i, reorderCards.get(cardOrder.get(i)));
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
            warpWorldService.beginNextPendingLibraryBottomReorder(gameData);
            return;
        }
        if (reorderedToBottom && gameData.warpWorldOperation.sourceName != null) {
            warpWorldService.finalizePendingWarpWorld(gameData);
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
        boolean reorderRemainingToTop = librarySearch.reorderRemainingToTop();
        boolean shuffleAfterSelection = librarySearch.shuffleAfterSelection();
        LibrarySearchDestination destination = librarySearch.destination() != null
                ? librarySearch.destination()
                : LibrarySearchDestination.HAND;
        boolean toBattlefield = destination == LibrarySearchDestination.BATTLEFIELD
                || destination == LibrarySearchDestination.BATTLEFIELD_TAPPED;
        boolean toBattlefieldTapped = destination == LibrarySearchDestination.BATTLEFIELD_TAPPED;
        boolean toGraveyard = destination == LibrarySearchDestination.GRAVEYARD;
        Set<CardType> filterCardTypes = librarySearch.filterCardTypes();

        UUID deckOwnerId = targetPlayerId != null ? targetPlayerId : playerId;
        UUID handOwnerId = targetPlayerId != null ? targetPlayerId : playerId;

        gameData.interaction.clearAwaitingInput();
        gameData.interaction.clearLibrarySearch();

        List<Card> deck = gameData.playerDecks.get(deckOwnerId);

        if (reorderRemainingToBottom || reorderRemainingToTop) {
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
                if (destination == LibrarySearchDestination.EXILE_IMPRINT) {
                    gameData.playerExiledCards.get(playerId).add(chosenCard);
                    UUID sourcePermanentId = gameData.imprintSourcePermanentId;
                    if (sourcePermanentId != null) {
                        gameQueryService.setImprintedCardOnPermanent(gameData, sourcePermanentId, chosenCard);
                        gameData.imprintSourcePermanentId = null;
                    }
                } else if (destination == LibrarySearchDestination.EXILE) {
                    gameData.playerExiledCards.get(deckOwnerId).add(chosenCard);
                } else if (toBattlefield) {
                    Permanent perm = new Permanent(chosenCard);
                    battlefieldEntryService.putPermanentOntoBattlefield(gameData, playerId, perm);
                    if (toBattlefieldTapped) {
                        perm.tap();
                    }
                    if (chosenCard.getType() == CardType.CREATURE) {
                        battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, playerId, chosenCard, null, false);
                    }
                    if (chosenCard.getType() == CardType.PLANESWALKER && chosenCard.getLoyalty() != null) {
                        perm.setLoyaltyCounters(chosenCard.getLoyalty());
                        perm.setSummoningSick(false);
                    }
                    if (!gameData.interaction.isAwaitingInput()) {
                        legendRuleService.checkLegendRule(gameData, playerId);
                    }
                } else {
                    gameData.playerHands.get(handOwnerId).add(chosenCard);
                }
                for (int i = 0; i < sourceCards.size(); i++) {
                    if (sourceCards.get(i).getId().equals(chosenCard.getId())) {
                        sourceCards.remove(i);
                        break;
                    }
                }
            }

            // Log the result
            String logEntry;
            if (destination == LibrarySearchDestination.EXILE_IMPRINT) {
                logEntry = chosenCard == null
                        ? player.getUsername() + "'s imprint ability does nothing."
                        : player.getUsername() + " exiles a card face down.";
            } else if (destination == LibrarySearchDestination.EXILE) {
                logEntry = chosenCard == null
                        ? player.getUsername() + " does not exile a card."
                        : player.getUsername() + " exiles " + chosenCard.getName() + ".";
            } else if (toBattlefield) {
                logEntry = chosenCard == null
                        ? player.getUsername() + " puts no card onto the battlefield."
                        : chosenCard.getName() + " enters the battlefield under " + player.getUsername() + "'s control.";
            } else {
                logEntry = chosenCard == null
                        ? player.getUsername() + " does not reveal a creature card."
                        : player.getUsername() + " reveals " + chosenCard.getName() + " and puts it into their hand.";
            }
            gameBroadcastService.logAndBroadcast(gameData, logEntry);

            // If ETB or legend rule caused awaiting input, defer remaining card reorder
            if (gameData.interaction.isAwaitingInput()) {
                if (!sourceCards.isEmpty()) {
                    gameData.pendingLibraryBottomReorders.addLast(
                            new LibraryBottomReorderRequest(deckOwnerId, new ArrayList<>(sourceCards)));
                }
                return;
            }

            if (sourceCards.size() > 1) {
                boolean toBottom = !reorderRemainingToTop;
                UUID reorderPlayerId = reorderRemainingToTop ? playerId : deckOwnerId;
                gameData.interaction.beginLibraryReorder(reorderPlayerId, sourceCards, toBottom, deckOwnerId);
                List<CardView> cardViews = sourceCards.stream().map(cardViewFactory::create).toList();
                String reorderPrompt = toBottom
                        ? "Put these cards on the bottom of your library in any order (first chosen will be closest to the top)."
                        : "Put these cards back on top of your library in any order (top to bottom).";
                sessionManager.sendToPlayer(reorderPlayerId, new ReorderLibraryCardsMessage(
                        cardViews,
                        reorderPrompt
                ));
                return;
            } else if (sourceCards.size() == 1) {
                if (reorderRemainingToTop) {
                    deck.addFirst(sourceCards.getFirst());
                } else {
                    deck.add(sourceCards.getFirst());
                }
            }

            if (toBattlefield) {
                stateBasedActionService.performStateBasedActions(gameData);
            }

            turnProgressionService.resolveAutoPass(gameData);
            return;
        }

        if (cardIndex == -1) {
            if (!canFailToFind) {
                throw new IllegalStateException("Cannot fail to find with an unrestricted search");
            }
            if (shuffleAfterSelection) {
                LibraryShuffleHelper.shuffleLibrary(gameData, deckOwnerId);
            }
            String logEntry = shuffleAfterSelection
                    ? player.getUsername() + " chooses not to take a card. Library is shuffled."
                    : player.getUsername() + " chooses not to take a card.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} declines to take a card from library", gameData.id, player.getUsername());
            // Per ruling: if you find only one basic land with Cultivate, it must go to
            // the battlefield tapped — skipping the battlefield pick means finding zero,
            // so clear the pending hand search and shuffle.
            if (gameData.pendingBasicLandToHandSearch) {
                gameData.pendingBasicLandToHandSearch = false;
                LibraryShuffleHelper.shuffleLibrary(gameData, deckOwnerId);
                String shuffleLog = player.getUsername() + "'s library is shuffled.";
                gameBroadcastService.logAndBroadcast(gameData, shuffleLog);
            }
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

        if (destination == LibrarySearchDestination.EXILE) {
            gameData.playerExiledCards.get(playerId).add(chosenCard);
            if (shuffleAfterSelection) {
                LibraryShuffleHelper.shuffleLibrary(gameData, deckOwnerId);
            }

            String logMsg = shuffleAfterSelection
                    ? player.getUsername() + " exiles a card face down. Library is shuffled."
                    : player.getUsername() + " exiles a card face down.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            log.info("Game {} - {} exiles {} from library search", gameData.id, player.getUsername(), chosenCard.getName());

            if (gameData.pendingOpponentExileChoice != null) {
                PendingOpponentExileChoice pending = gameData.pendingOpponentExileChoice;
                gameData.pendingOpponentExileChoice = null;

                UUID opponentId = null;
                for (UUID pid : gameData.orderedPlayerIds) {
                    if (!pid.equals(pending.controllerId())) {
                        opponentId = pid;
                        break;
                    }
                }

                if (opponentId != null) {
                    String controllerName = gameData.playerIdToName.get(pending.controllerId());
                    String prompt = "Let " + controllerName + " put the exiled card into their hand? If you decline, they draw "
                            + pending.drawCountOnDecline() + " cards.";
                    gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                            chosenCard, opponentId,
                            List.of(new OpponentMayReturnExiledCardOrDrawEffect(pending.drawCountOnDecline())),
                            prompt, chosenCard.getId()
                    ));
                    playerInputService.processNextMayAbility(gameData);
                }
            } else {
                turnProgressionService.resolveAutoPass(gameData);
            }
            return;
        }

        if (destination == LibrarySearchDestination.EXILE_PLAYABLE) {
            gameData.playerExiledCards.get(playerId).add(chosenCard);
            gameData.exilePlayPermissions.put(chosenCard.getId(), playerId);
            if (shuffleAfterSelection) {
                LibraryShuffleHelper.shuffleLibrary(gameData, deckOwnerId);
            }

            String logMsg = shuffleAfterSelection
                    ? player.getUsername() + " exiles a card face down. Library is shuffled."
                    : player.getUsername() + " exiles a card face down.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            log.info("Game {} - {} exiles {} from library search (with play permission)", gameData.id, player.getUsername(), chosenCard.getName());

            turnProgressionService.resolveAutoPass(gameData);
            return;
        }

        if (destination == LibrarySearchDestination.TOP_OF_LIBRARY) {
            // Shuffle library first, then put the card on top (MTG rule: "shuffle and put on top")
            if (shuffleAfterSelection) {
                LibraryShuffleHelper.shuffleLibrary(gameData, deckOwnerId);
            }
            deck.addFirst(chosenCard);
            String topLog = player.getUsername() + " reveals " + chosenCard.getName()
                    + " and puts it on top of their library. Library is shuffled.";
            gameBroadcastService.logAndBroadcast(gameData, topLog);
            log.info("Game {} - {} searches library and puts {} on top",
                    gameData.id, player.getUsername(), chosenCard.getName());
            turnProgressionService.resolveAutoPass(gameData);
            return;
        } else if (toGraveyard) {
            graveyardService.addCardToGraveyard(gameData, deckOwnerId, chosenCard);
        } else if (destination == LibrarySearchDestination.HAND) {
            gameData.playerHands.get(handOwnerId).add(chosenCard);
        } else if (destination == LibrarySearchDestination.EXILE_IMPRINT) {
            gameData.playerExiledCards.get(playerId).add(chosenCard);
            UUID sourcePermanentId = gameData.imprintSourcePermanentId;
            if (sourcePermanentId != null) {
                gameQueryService.setImprintedCardOnPermanent(gameData, sourcePermanentId, chosenCard);
                gameData.imprintSourcePermanentId = null;
            }
        } else {
            Permanent perm = new Permanent(chosenCard);
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, handOwnerId, perm);
            if (toBattlefieldTapped) {
                perm.tap();
            }

            String battlefieldOwner = gameData.playerIdToName.get(handOwnerId);
            String entersLog = toBattlefieldTapped
                    ? chosenCard.getName() + " enters the battlefield tapped under " + battlefieldOwner + "'s control."
                    : chosenCard.getName() + " enters the battlefield under " + battlefieldOwner + "'s control.";
            gameBroadcastService.logAndBroadcast(gameData, entersLog);

            if (chosenCard.getType() == CardType.CREATURE) {
                battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, handOwnerId, chosenCard, null, false);
            }
            if (chosenCard.getType() == CardType.PLANESWALKER && chosenCard.getLoyalty() != null) {
                perm.setLoyaltyCounters(chosenCard.getLoyalty());
                perm.setSummoningSick(false);
            }
            if (!gameData.interaction.isAwaitingInput()) {
                legendRuleService.checkLegendRule(gameData, handOwnerId);
            }
        }

        if (remainingCount > 1) {
            int newRemaining = remainingCount - 1;
            List<Card> newSearchCards = filterCardTypes != null
                    ? deck.stream().filter(c -> filterCardTypes.contains(c.getType()) || c.getAdditionalTypes().stream().anyMatch(filterCardTypes::contains)).toList()
                    : new ArrayList<>(deck);

            if (newSearchCards.isEmpty()) {
                // No more matching cards — shuffle and finish
                LibraryShuffleHelper.shuffleLibrary(gameData, deckOwnerId);
                String logMsg;
                if (targetPlayerId != null) {
                    String targetName = gameData.playerIdToName.get(targetPlayerId);
                    logMsg = player.getUsername() + " finds no more matching cards in " + targetName + "'s library. Library is shuffled.";
                } else {
                    logMsg = player.getUsername() + " finds no more matching cards. Library is shuffled.";
                }
                gameBroadcastService.logAndBroadcast(gameData, logMsg);
                if (toBattlefield) {
                    stateBasedActionService.performStateBasedActions(gameData);
                }
                turnProgressionService.resolveAutoPass(gameData);
                return;
            }

            String prompt;
            if (targetPlayerId != null) {
                String targetName = gameData.playerIdToName.get(targetPlayerId);
                String destinationDesc = toGraveyard ? "their graveyard" : "their hand";
                prompt = "Search " + targetName + "'s library for a card to put into " + destinationDesc + " (" + newRemaining + " remaining).";
            } else {
                String destinationDesc = toBattlefieldTapped ? "onto the battlefield tapped"
                        : toBattlefield ? "onto the battlefield" : "into your hand";
                prompt = "Search your library for a matching card to put " + destinationDesc + " (" + newRemaining + " remaining).";
            }

            gameData.interaction.beginLibrarySearch(LibrarySearchParams.builder(playerId, new ArrayList<>(newSearchCards))
                    .targetPlayerId(targetPlayerId)
                    .remainingCount(newRemaining)
                    .canFailToFind(toGraveyard || canFailToFind)
                    .destination(destination)
                    .filterCardTypes(filterCardTypes)
                    .build());

            List<CardView> cardViews = newSearchCards.stream().map(cardViewFactory::create).toList();
            sessionManager.sendToPlayer(playerId, new ChooseCardFromLibraryMessage(
                    cardViews,
                    prompt,
                    toGraveyard || canFailToFind
            ));

            log.info("Game {} - {} picks from library, {} remaining", gameData.id, player.getUsername(), newRemaining);
            return;
        }

        if (shuffleAfterSelection) {
            LibraryShuffleHelper.shuffleLibrary(gameData, deckOwnerId);
        }

        String destinationText = switch (destination) {
            case BATTLEFIELD -> "onto the battlefield";
            case BATTLEFIELD_TAPPED -> "onto the battlefield tapped";
            case HAND -> "into their hand";
            case EXILE_IMPRINT -> "into exile (imprint)";
            case EXILE, EXILE_PLAYABLE -> "into exile";
            case TOP_OF_LIBRARY -> "on top of their library";
            case GRAVEYARD -> "into their graveyard";
        };
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

        if (toBattlefield) {
            stateBasedActionService.performStateBasedActions(gameData);
        }

        if (startPendingBasicLandToHandSearch(gameData, playerId)) return;
        turnProgressionService.resolveAutoPass(gameData);
    }
    /**
     * If a pending basic-land-to-hand search is queued (e.g. Cultivate second pick),
     * starts the follow-up library search and returns true. Otherwise returns false.
     */
    private boolean startPendingBasicLandToHandSearch(GameData gameData, UUID playerId) {
        if (!gameData.pendingBasicLandToHandSearch) return false;
        gameData.pendingBasicLandToHandSearch = false;

        List<Card> deck = gameData.playerDecks.get(playerId);
        String playerName = gameData.playerIdToName.get(playerId);

        List<Card> basicLands = deck.stream()
                .filter(card -> card.getType() == CardType.LAND && card.getSupertypes().contains(CardSupertype.BASIC))
                .toList();

        if (basicLands.isEmpty()) {
            LibraryShuffleHelper.shuffleLibrary(gameData, playerId);
            String logMsg = playerName + " finds no more basic land cards. Library is shuffled.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            return false;
        }

        String prompt = "Search your library for a basic land card to put into your hand.";
        LibrarySearchParams params = LibrarySearchParams.builder(playerId, new ArrayList<>(basicLands))
                .reveals(true)
                .canFailToFind(true)
                .destination(LibrarySearchDestination.HAND)
                .build();

        gameData.interaction.beginLibrarySearch(params);
        List<CardView> cardViews = basicLands.stream().map(cardViewFactory::create).toList();
        sessionManager.sendToPlayer(playerId, new ChooseCardFromLibraryMessage(cardViews, prompt, true));
        return true;
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

        if (libraryRevealChoice.selectedToHand()) {
            resolveRevealChoiceToHand(gameData, controllerId, playerName, selectedCards, remainingCards,
                    libraryRevealChoice.reorderRemainingToBottom());
            return;
        }

        // Put selected cards onto the battlefield
        Set<CardType> enterTappedTypesSnapshot = EnumSet.noneOf(CardType.class);
        enterTappedTypesSnapshot.addAll(battlefieldEntryService.snapshotEnterTappedTypes(gameData));
        for (Card card : selectedCards) {
            Permanent perm = new Permanent(card);
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, perm, enterTappedTypesSnapshot);

            String logEntry = card.getName() + " enters the battlefield under " + playerName + "'s control.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);

            if (card.getType() == CardType.CREATURE) {
                battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, controllerId, card, null, false);
            }
            if (card.getType() == CardType.PLANESWALKER && card.getLoyalty() != null) {
                perm.setLoyaltyCounters(card.getLoyalty());
                perm.setSummoningSick(false);
            }
            if (!gameData.interaction.isAwaitingInput()) {
                legendRuleService.checkLegendRule(gameData, controllerId);
            }
        }

        // Handle remaining cards based on destination
        if (libraryRevealChoice.remainingToGraveyard()) {
            for (Card card : remainingCards) {
                graveyardService.addCardToGraveyard(gameData, controllerId, card);
            }

            if (selectedCards.isEmpty()) {
                String logEntry = playerName + " puts no cards onto the battlefield. The rest are put into their graveyard.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
            } else {
                String names = selectedCards.stream().map(Card::getName).reduce((a, b) -> a + ", " + b).orElse("");
                String logEntry = playerName + " puts " + names + " onto the battlefield. The rest are put into their graveyard.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
            }
        } else {
            List<Card> deck = gameData.playerDecks.get(controllerId);
            deck.addAll(remainingCards);
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);

            if (selectedCards.isEmpty()) {
                String logEntry = playerName + " puts no cards onto the battlefield. Library is shuffled.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
            } else {
                String names = selectedCards.stream().map(Card::getName).reduce((a, b) -> a + ", " + b).orElse("");
                String logEntry = playerName + " puts " + names + " onto the battlefield. Library is shuffled.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
            }
        }

        log.info("Game {} - {} resolves library reveal choice, {} cards to battlefield", gameData.id, playerName, selectedCards.size());

        stateBasedActionService.performStateBasedActions(gameData);
        turnProgressionService.resolveAutoPass(gameData);
    }

    private void resolveRevealChoiceToHand(GameData gameData, UUID controllerId, String playerName,
                                              List<Card> selectedCards, List<Card> remainingCards,
                                              boolean reorderRemainingToBottom) {
        // Put selected cards into hand
        for (Card card : selectedCards) {
            gameData.playerHands.get(controllerId).add(card);
        }

        // Log the result
        if (selectedCards.isEmpty()) {
            String logEntry = playerName + " does not reveal any creature cards.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
        } else {
            String names = selectedCards.stream().map(Card::getName).reduce((a, b) -> a + ", " + b).orElse("");
            String logEntry = playerName + " reveals " + names + " and puts them into their hand.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
        }

        // Handle remaining cards
        if (reorderRemainingToBottom && remainingCards.size() > 1) {
            gameData.interaction.beginLibraryReorder(controllerId, remainingCards, true);
            List<CardView> cardViews = remainingCards.stream().map(cardViewFactory::create).toList();
            sessionManager.sendToPlayer(controllerId, new ReorderLibraryCardsMessage(
                    cardViews,
                    "Put these cards on the bottom of your library in any order (first chosen will be closest to the top)."
            ));
            log.info("Game {} - {} reveals {} creature cards to hand, reordering {} remaining",
                    gameData.id, playerName, selectedCards.size(), remainingCards.size());
            return;
        }

        if (!remainingCards.isEmpty()) {
            List<Card> deck = gameData.playerDecks.get(controllerId);
            deck.addAll(remainingCards);
        }

        log.info("Game {} - {} reveals {} creature cards to hand", gameData.id, playerName, selectedCards.size());
        turnProgressionService.resolveAutoPass(gameData);
    }

}



