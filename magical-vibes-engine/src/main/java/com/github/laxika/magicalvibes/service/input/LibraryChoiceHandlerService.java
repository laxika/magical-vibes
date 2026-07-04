package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.InteractionContext;
import com.github.laxika.magicalvibes.model.LibraryBottomReorderRequest;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingKarnScionExileReturn;
import com.github.laxika.magicalvibes.model.PendingKarnScionRevealChoice;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.PendingOpponentExileChoice;
import com.github.laxika.magicalvibes.model.PendingSphinxAmbassadorChoice;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TargetType;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentMayReturnExiledCardOrDrawEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.networking.message.ReorderLibraryCardsMessage;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import com.github.laxika.magicalvibes.service.WarpWorldService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.battlefield.LegendRuleService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.state.StateBasedActionService;
import com.github.laxika.magicalvibes.service.turn.TurnProgressionService;
import com.github.laxika.magicalvibes.service.effect.EffectResolutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import com.github.laxika.magicalvibes.model.CounterType;

@Slf4j
@Service
@RequiredArgsConstructor
public class LibraryChoiceHandlerService {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GraveyardService graveyardService;
    private final WarpWorldService warpWorldService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final LegendRuleService legendRuleService;
    private final StateBasedActionService stateBasedActionService;
    private final GameBroadcastService gameBroadcastService;
    private final TurnProgressionService turnProgressionService;
    private final PlayerInputService playerInputService;
    private final EffectResolutionService effectResolutionService;
    private final ExileService exileService;
    private final com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry interactionHandlerRegistry;
    private final TriggerCollectionService triggerCollectionService;


    public void handleLibraryCardChosen(GameData gameData, Player player, int cardIndex) {
        if (!gameData.interaction.isAwaitingInput(AwaitingInput.LIBRARY_SEARCH)) {
            throw new IllegalStateException("Not awaiting library search");
        }
        PendingInteraction.LibrarySearch activeSearch =
                gameData.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        LibrarySearchParams librarySearch = activeSearch != null ? activeSearch.params() : null;
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
                || destination == LibrarySearchDestination.BATTLEFIELD_TAPPED
                || destination == LibrarySearchDestination.BATTLEFIELD_ATTACHED_TO_PLAYER;
        boolean toBattlefieldTapped = destination == LibrarySearchDestination.BATTLEFIELD_TAPPED;
        boolean toGraveyard = destination == LibrarySearchDestination.GRAVEYARD;
        Set<CardType> filterCardTypes = librarySearch.filterCardTypes();
        String filterCardName = librarySearch.filterCardName();
        com.github.laxika.magicalvibes.model.filter.CardPredicate filterPredicate = librarySearch.filterPredicate();
        List<Card> accumulatedCards = librarySearch.accumulatedCards() != null
                ? new ArrayList<>(librarySearch.accumulatedCards()) : new ArrayList<>();

        UUID deckOwnerId = targetPlayerId != null ? targetPlayerId : playerId;
        UUID handOwnerId = targetPlayerId != null ? targetPlayerId : playerId;

        gameData.interaction.clearAwaitingInput();

        List<Card> deck = gameData.playerDecks.get(deckOwnerId);

        if (reorderRemainingToBottom || reorderRemainingToTop) {
            if (sourceCards == null) {
                throw new IllegalStateException("Missing source cards for revealed-card choice");
            }

            // Sunbird's Invocation: cast chosen card without paying, rest to bottom in random order
            if (destination == LibrarySearchDestination.CAST_WITHOUT_PAYING) {
                handleCastWithoutPayingChoice(gameData, player, cardIndex, canFailToFind,
                        searchCards, sourceCards, deck);
                return;
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
                    exileService.exileCard(gameData, playerId, chosenCard);
                    UUID sourcePermanentId = gameData.imprintSourcePermanentId;
                    if (sourcePermanentId != null) {
                        gameQueryService.setImprintedCardOnPermanent(gameData, sourcePermanentId, chosenCard);
                        gameData.imprintSourcePermanentId = null;
                    }
                } else if (destination == LibrarySearchDestination.EXILE) {
                    exileService.exileCard(gameData, deckOwnerId, chosenCard);
                } else if (toBattlefield) {
                    Permanent perm = new Permanent(chosenCard);
                    battlefieldEntryService.putPermanentOntoBattlefield(gameData, playerId, perm);
                    if (toBattlefieldTapped) {
                        perm.tap();
                    }
                    if (destination == LibrarySearchDestination.BATTLEFIELD_ATTACHED_TO_PLAYER && librarySearch.attachToPlayerId() != null) {
                        perm.setAttachedTo(librarySearch.attachToPlayerId());
                    }
                    if (chosenCard.hasType(CardType.CREATURE)) {
                        battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, playerId, chosenCard, null, false);
                    }
                    if (chosenCard.hasType(CardType.PLANESWALKER) && chosenCard.getLoyalty() != null) {
                        perm.setCounterCount(CounterType.LOYALTY, chosenCard.getLoyalty());
                        perm.setSummoningSick(false);
                    }
                    if (!gameData.interaction.isAwaitingInput()) {
                        legendRuleService.checkLegendRule(gameData, playerId);
                    }
                } else {
                    gameData.addCardToHand(handOwnerId, chosenCard);
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
                String reorderPrompt = toBottom
                        ? "Put these cards on the bottom of your library in any order (first chosen will be closest to the top)."
                        : "Put these cards back on top of your library in any order (top to bottom).";
                interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryReorder(
                        reorderPlayerId, sourceCards, toBottom, deckOwnerId, reorderPrompt));
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
            // CR 608.2f: Place any accumulated battlefield cards before finishing
            if (!accumulatedCards.isEmpty() && toBattlefield) {
                placeCardsOnBattlefieldSimultaneously(gameData, accumulatedCards, handOwnerId, toBattlefieldTapped);
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
            if (startPendingEachPlayerBasicLandSearch(gameData)) return;
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
            exileService.exileCard(gameData, playerId, chosenCard);
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
            exileService.exileCard(gameData, playerId, chosenCard);
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

        if (destination == LibrarySearchDestination.SPHINX_AMBASSADOR) {
            // Card is "set aside" (already removed from deck above).
            // Update the pending choice with the selected card, then prompt the opponent to name a card.
            PendingSphinxAmbassadorChoice pending = gameData.pollPendingInteraction(PendingSphinxAmbassadorChoice.class);
            gameData.queueInteraction(new PendingSphinxAmbassadorChoice(
                    chosenCard, pending.controllerId(), pending.targetPlayerId(), pending.sourceCard()));

            UUID opponentId = pending.targetPlayerId();
            String controllerName = gameData.playerIdToName.get(pending.controllerId());
            String opponentName = gameData.playerIdToName.get(opponentId);

            String logMsg = controllerName + " selects a card from " + opponentName + "'s library.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            log.info("Game {} - {} selects {} from {}'s library for Sphinx Ambassador",
                    gameData.id, controllerName, chosenCard.getName(), opponentName);

            // Prompt the opponent to name a card
            playerInputService.beginSphinxAmbassadorCardNameChoice(gameData, opponentId, pending.controllerId());
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
            exileService.exileCard(gameData, playerId, chosenCard);
            UUID sourcePermanentId = gameData.imprintSourcePermanentId;
            if (sourcePermanentId != null) {
                gameQueryService.setImprintedCardOnPermanent(gameData, sourcePermanentId, chosenCard);
                gameData.imprintSourcePermanentId = null;
            }
        } else if (destination == LibrarySearchDestination.BATTLEFIELD_ATTACHED_TO_PLAYER) {
            Permanent perm = new Permanent(chosenCard);
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, playerId, perm);
            if (librarySearch.attachToPlayerId() != null) {
                perm.setAttachedTo(librarySearch.attachToPlayerId());
            }
        } else {
            if (remainingCount > 1) {
                // CR 608.2f: Accumulate for simultaneous battlefield entry
                accumulatedCards.add(chosenCard);
            } else {
                // Final pick (or single pick) — place all accumulated + current simultaneously
                List<Card> allCards = new ArrayList<>(accumulatedCards);
                allCards.add(chosenCard);
                placeCardsOnBattlefieldSimultaneously(gameData, allCards, handOwnerId, toBattlefieldTapped);
            }
        }

        if (remainingCount > 1) {
            int newRemaining = remainingCount - 1;
            List<Card> newSearchCards;
            if (filterCardName != null) {
                newSearchCards = deck.stream().filter(c -> filterCardName.equals(c.getName())).toList();
            } else if (filterPredicate != null) {
                final com.github.laxika.magicalvibes.model.filter.CardPredicate fp = filterPredicate;
                newSearchCards = deck.stream().filter(c -> predicateEvaluationService.matchesCardPredicate(c, fp, null)).toList();
            } else if (filterCardTypes != null) {
                newSearchCards = deck.stream().filter(c -> filterCardTypes.contains(c.getType()) || c.getAdditionalTypes().stream().anyMatch(filterCardTypes::contains)).toList();
            } else {
                newSearchCards = new ArrayList<>(deck);
            }

            if (newSearchCards.isEmpty()) {
                // CR 608.2f: Place any accumulated battlefield cards before finishing
                if (!accumulatedCards.isEmpty() && toBattlefield) {
                    placeCardsOnBattlefieldSimultaneously(gameData, accumulatedCards, handOwnerId, toBattlefieldTapped);
                }
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

            interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibrarySearch(
                    LibrarySearchParams.builder(playerId, new ArrayList<>(newSearchCards))
                    .targetPlayerId(targetPlayerId)
                    .remainingCount(newRemaining)
                    .canFailToFind(toGraveyard || canFailToFind)
                    .destination(destination)
                    .filterCardTypes(filterCardTypes)
                    .filterCardName(filterCardName)
                    .filterPredicate(filterPredicate)
                    .accumulatedCards(accumulatedCards)
                    .build(),
                    prompt, toGraveyard || canFailToFind));

            log.info("Game {} - {} picks from library, {} remaining", gameData.id, player.getUsername(), newRemaining);
            return;
        }

        if (shuffleAfterSelection) {
            LibraryShuffleHelper.shuffleLibrary(gameData, deckOwnerId);
        }

        // When simultaneous placement was done, individual entry logs were already emitted
        if (!accumulatedCards.isEmpty() && toBattlefield) {
            if (shuffleAfterSelection) {
                String shuffleLog = player.getUsername() + "'s library is shuffled.";
                gameBroadcastService.logAndBroadcast(gameData, shuffleLog);
            }
        } else {
            String destinationText = switch (destination) {
                case BATTLEFIELD -> "onto the battlefield";
                case BATTLEFIELD_TAPPED -> "onto the battlefield tapped";
                case BATTLEFIELD_ATTACHED_TO_PLAYER -> "onto the battlefield";
                case HAND -> "into their hand";
                case EXILE_IMPRINT -> "into exile (imprint)";
                case EXILE, EXILE_PLAYABLE -> "into exile";
                case TOP_OF_LIBRARY -> "on top of their library";
                case GRAVEYARD -> "into their graveyard";
                case SPHINX_AMBASSADOR -> throw new IllegalStateException("SPHINX_AMBASSADOR should be handled earlier");
                case CAST_WITHOUT_PAYING -> throw new IllegalStateException("CAST_WITHOUT_PAYING should be handled earlier");
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
        }

        if (toBattlefield) {
            stateBasedActionService.performStateBasedActions(gameData);
        }

        if (startPendingBasicLandToHandSearch(gameData, playerId)) return;
        if (startPendingCardToGraveyardSearch(gameData, playerId)) return;
        if (startPendingEachPlayerBasicLandSearch(gameData)) return;
        turnProgressionService.resolveAutoPass(gameData);
    }
    /**
     * Places multiple cards onto the battlefield simultaneously per CR 608.2f.
     * All permanents are added first, then ETB triggers are processed after all are on the battlefield.
     */
    private void placeCardsOnBattlefieldSimultaneously(GameData gameData, List<Card> cards,
                                                        UUID ownerId, boolean tapped) {
        List<Permanent> permanents = new ArrayList<>();
        List<Card> placedCards = new ArrayList<>();
        String ownerName = gameData.playerIdToName.get(ownerId);

        // Snapshot enter-tapped types ONCE before any permanent enters (CR 608.2f)
        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);

        // Grafdigger's Cage etc.: matching cards (e.g. creature cards) in libraries can't enter the
        // battlefield. Any blocked card stays in the library, which is then shuffled.
        boolean anyBlocked = false;

        // Phase 1: Place all permanents on the battlefield simultaneously
        for (Card card : cards) {
            if (gameQueryService.isCardBlockedFromEnteringFromZone(gameData, card, Zone.LIBRARY)) {
                gameData.playerDecks.computeIfAbsent(ownerId, k -> new ArrayList<>()).add(card);
                anyBlocked = true;
                gameBroadcastService.logAndBroadcast(gameData, card.getName()
                        + " can't enter the battlefield from a library; it stays in the library.");
                continue;
            }
            Permanent perm = new Permanent(card);
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, ownerId, perm, enterTappedTypes);
            if (tapped) {
                perm.tap();
            }
            permanents.add(perm);
            placedCards.add(card);

            String entersLog = tapped
                    ? card.getName() + " enters the battlefield tapped under " + ownerName + "'s control."
                    : card.getName() + " enters the battlefield under " + ownerName + "'s control.";
            gameBroadcastService.logAndBroadcast(gameData, entersLog);
        }

        if (anyBlocked) {
            java.util.Collections.shuffle(gameData.playerDecks.get(ownerId));
        }

        // Phase 2: Process ETB triggers after all permanents are on the battlefield (CR 608.2f)
        for (int i = 0; i < placedCards.size(); i++) {
            Card card = placedCards.get(i);
            Permanent perm = permanents.get(i);

            if (card.hasType(CardType.CREATURE)) {
                battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, ownerId, card, null, false);
            }
            if (card.hasType(CardType.PLANESWALKER) && card.getLoyalty() != null) {
                perm.setCounterCount(CounterType.LOYALTY, card.getLoyalty());
                perm.setSummoningSick(false);
            }
        }

        if (!gameData.interaction.isAwaitingInput()) {
            legendRuleService.checkLegendRule(gameData, ownerId);
        }
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
                .filter(card -> card.hasType(CardType.LAND) && card.getSupertypes().contains(CardSupertype.BASIC))
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

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibrarySearch(params, prompt, true));
        return true;
    }

    /**
     * If a pending unrestricted card-to-graveyard search is queued (e.g. Final Parting second pick),
     * starts the follow-up library search and returns true. Otherwise returns false.
     */
    private boolean startPendingCardToGraveyardSearch(GameData gameData, UUID playerId) {
        if (!gameData.pendingCardToGraveyardSearch) return false;
        gameData.pendingCardToGraveyardSearch = false;

        List<Card> deck = gameData.playerDecks.get(playerId);
        String playerName = gameData.playerIdToName.get(playerId);

        if (deck.isEmpty()) {
            LibraryShuffleHelper.shuffleLibrary(gameData, playerId);
            String logMsg = playerName + " finds no more cards. Library is shuffled.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            return false;
        }

        String prompt = "Search your library for a card to put into your graveyard.";
        LibrarySearchParams params = LibrarySearchParams.builder(playerId, new ArrayList<>(deck))
                .reveals(false)
                .canFailToFind(false)
                .destination(LibrarySearchDestination.GRAVEYARD)
                .build();

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibrarySearch(params, prompt, false));
        return true;
    }

    /**
     * If a pending "each player searches for a basic land to battlefield" queue is non-empty,
     * starts the next player's library search and returns true. Otherwise returns false.
     * Respects {@code pendingEachPlayerBasicLandSearchTapped} for the destination.
     * Used by Field of Ruin, Old-Growth Dryads.
     */
    private boolean startPendingEachPlayerBasicLandSearch(GameData gameData) {
        if (gameData.pendingEachPlayerBasicLandSearchQueue.isEmpty()) return false;

        LibrarySearchDestination destination = gameData.pendingEachPlayerBasicLandSearchTapped
                ? LibrarySearchDestination.BATTLEFIELD_TAPPED
                : LibrarySearchDestination.BATTLEFIELD;
        String prompt = gameData.pendingEachPlayerBasicLandSearchTapped
                ? "You may search your library for a basic land card and put it onto the battlefield tapped."
                : "Search your library for a basic land card and put it onto the battlefield.";

        while (!gameData.pendingEachPlayerBasicLandSearchQueue.isEmpty()) {
            UUID nextPlayerId = gameData.pendingEachPlayerBasicLandSearchQueue.pollFirst();
            String playerName = gameData.playerIdToName.get(nextPlayerId);

            List<Card> deck = gameData.playerDecks.get(nextPlayerId);
            if (deck == null || deck.isEmpty()) {
                String logMsg = playerName + " searches their library but it is empty. Library is shuffled.";
                gameBroadcastService.logAndBroadcast(gameData, logMsg);
                continue;
            }

            List<Card> basicLands = deck.stream()
                    .filter(card -> card.hasType(CardType.LAND) && card.getSupertypes().contains(CardSupertype.BASIC))
                    .toList();

            if (basicLands.isEmpty()) {
                LibraryShuffleHelper.shuffleLibrary(gameData, nextPlayerId);
                String logMsg = playerName + " searches their library but finds no basic land cards. Library is shuffled.";
                gameBroadcastService.logAndBroadcast(gameData, logMsg);
                continue;
            }

            LibrarySearchParams params = LibrarySearchParams.builder(nextPlayerId, new ArrayList<>(basicLands))
                    .reveals(false)
                    .canFailToFind(true)
                    .destination(destination)
                    .build();

            interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibrarySearch(params, prompt, true));

            String logMsg = playerName + " searches their library.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            return true;
        }

        return false;
    }

    public void handleLibraryRevealChoice(GameData gameData, Player player, List<UUID> cardIds) {
        PendingInteraction.LibraryRevealChoice libraryRevealChoice =
                gameData.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        if (libraryRevealChoice == null || !player.getId().equals(libraryRevealChoice.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        List<UUID> validIds = libraryRevealChoice.validCardIds();
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

        // Karn, Scion of Urza +1: opponent chose which card goes to controller's hand
        if (gameData.hasPendingInteraction(PendingKarnScionRevealChoice.class)) {
            handleKarnScionRevealChoice(gameData, allRevealedCards, cardIds);
            return;
        }

        // Karn, Scion of Urza -1: controller chose which silver-counter card to return
        if (gameData.hasPendingInteraction(PendingKarnScionExileReturn.class)) {
            handleKarnScionReturnFromExile(gameData, allRevealedCards, cardIds, controllerId);
            return;
        }

        // Punisher reveal (Sword-Point Diplomacy etc.): opponent chose which cards to deny (paying life)
        if (libraryRevealChoice.lifeCostPerSelection() > 0 && libraryRevealChoice.beneficiaryPlayerId() != null) {
            handlePunisherRevealChoice(gameData, allRevealedCards, cardIds,
                    libraryRevealChoice.beneficiaryPlayerId(), libraryRevealChoice.lifeCostPerSelection());
            return;
        }

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
                    libraryRevealChoice.reorderRemainingToBottom(), libraryRevealChoice.remainingToGraveyard());
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

            if (card.hasType(CardType.CREATURE)) {
                battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, controllerId, card, null, false);
            }
            if (card.hasType(CardType.PLANESWALKER) && card.getLoyalty() != null) {
                perm.setCounterCount(CounterType.LOYALTY, card.getLoyalty());
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
        } else if (libraryRevealChoice.randomRemainingToBottom()) {
            // Shuffle remaining cards and put them on the bottom of the library (Gishath, etc.)
            Collections.shuffle(remainingCards);
            List<Card> deck = gameData.playerDecks.get(controllerId);
            deck.addAll(remainingCards);

            if (selectedCards.isEmpty()) {
                String logEntry = playerName + " puts no cards onto the battlefield. The rest are put on the bottom of their library in a random order.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
            } else {
                String names = selectedCards.stream().map(Card::getName).reduce((a, b) -> a + ", " + b).orElse("");
                String logEntry = playerName + " puts " + names + " onto the battlefield. The rest are put on the bottom of their library in a random order.";
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
        resolveRevealChoiceToHand(gameData, controllerId, playerName, selectedCards, remainingCards,
                reorderRemainingToBottom, false);
    }

    private void resolveRevealChoiceToHand(GameData gameData, UUID controllerId, String playerName,
                                              List<Card> selectedCards, List<Card> remainingCards,
                                              boolean reorderRemainingToBottom, boolean remainingToGraveyard) {
        // Put selected cards into hand
        for (Card card : selectedCards) {
            gameData.addCardToHand(controllerId, card);
        }

        // Log the result
        if (remainingToGraveyard) {
            if (!selectedCards.isEmpty()) {
                String countWord = selectedCards.size() == 1 ? "one card" : selectedCards.size() + " cards";
                String logEntry = playerName + " puts " + countWord + " into their hand and the rest into their graveyard.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
            }
        } else if (selectedCards.isEmpty()) {
            String logEntry = playerName + " does not reveal any creature cards.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
        } else {
            String names = selectedCards.stream().map(Card::getName).reduce((a, b) -> a + ", " + b).orElse("");
            String logEntry = playerName + " reveals " + names + " and puts them into their hand.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
        }

        // Handle remaining cards
        if (remainingToGraveyard) {
            for (Card card : remainingCards) {
                graveyardService.addCardToGraveyard(gameData, controllerId, card);
            }
            log.info("Game {} - {} puts {} card(s) to hand, {} to graveyard", gameData.id, playerName, selectedCards.size(), remainingCards.size());

            // Resume resolving remaining effects on the same spell/ability
            // (e.g. Dark Bargain: "Look at top 3, put 2 to hand, rest to graveyard. Deals 2 damage to you.")
            if (gameData.pendingEffectResolutionEntry != null) {
                effectResolutionService.resolveEffectsFrom(gameData,
                        gameData.pendingEffectResolutionEntry,
                        gameData.pendingEffectResolutionIndex);
            }

            turnProgressionService.resolveAutoPass(gameData);
            return;
        }

        if (reorderRemainingToBottom && remainingCards.size() > 1) {
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryReorder(
                    controllerId, remainingCards, true, controllerId,
                    "Put these cards on the bottom of your library in any order (first chosen will be closest to the top)."));
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

    private void handleKarnScionRevealChoice(GameData gameData, List<Card> allRevealedCards, List<UUID> selectedCardIds) {
        UUID controllerId = gameData.pollPendingInteraction(PendingKarnScionRevealChoice.class).controllerId();

        String controllerName = gameData.playerIdToName.get(controllerId);

        // The opponent selected one card — that card goes to the controller's hand,
        // the other is exiled with a silver counter. Only use the first selection.
        UUID chosenId = selectedCardIds.isEmpty() ? null : selectedCardIds.getFirst();
        Card toHand = null;
        Card toExile = null;
        for (Card card : allRevealedCards) {
            if (toHand == null && card.getId().equals(chosenId)) {
                toHand = card;
            } else {
                toExile = card;
            }
        }

        if (toHand != null) {
            gameData.addCardToHand(controllerId, toHand);
            gameBroadcastService.logAndBroadcast(gameData,
                    controllerName + " puts " + toHand.getName() + " into their hand.");
        }

        if (toExile != null) {
            exileService.exileCard(gameData, controllerId, toExile);
            gameData.exiledCardsWithSilverCounters.add(toExile.getId());
            gameBroadcastService.logAndBroadcast(gameData,
                    toExile.getName() + " is exiled with a silver counter.");
        }

        log.info("Game {} - Karn Scion +1 resolved: {} to hand, {} exiled with silver counter",
                gameData.id,
                toHand != null ? toHand.getName() : "none",
                toExile != null ? toExile.getName() : "none");

        turnProgressionService.resolveAutoPass(gameData);
    }

    private void handleKarnScionReturnFromExile(GameData gameData, List<Card> allRevealedCards,
                                                 List<UUID> selectedCardIds, UUID controllerId) {
        gameData.clearPendingInteractions(PendingKarnScionExileReturn.class);

        String controllerName = gameData.playerIdToName.get(controllerId);

        // Find the selected card and return it to hand
        Set<UUID> selectedIds = new HashSet<>(selectedCardIds);
        for (Card card : allRevealedCards) {
            if (selectedIds.contains(card.getId())) {
                // Remove from exile zone
                gameData.removeFromExile(card.getId());
                gameData.exiledCardsWithSilverCounters.remove(card.getId());
                gameData.addCardToHand(controllerId, card);

                gameBroadcastService.logAndBroadcast(gameData,
                        controllerName + " returns " + card.getName() + " from exile to their hand.");
                log.info("Game {} - {} returns {} from exile (silver counter) to hand",
                        gameData.id, controllerName, card.getName());
                break;
            }
        }

        turnProgressionService.resolveAutoPass(gameData);
    }

    private void handlePunisherRevealChoice(GameData gameData, List<Card> allRevealedCards,
                                              List<UUID> selectedCardIds,
                                              UUID controllerId, int lifeCost) {

        String controllerName = gameData.playerIdToName.get(controllerId);

        // Determine the opponent (the player who made the choice)
        UUID opponentId = gameData.orderedPlayerIds.stream()
                .filter(id -> !id.equals(controllerId))
                .findFirst()
                .orElseThrow();
        String opponentName = gameData.playerIdToName.get(opponentId);

        // Validate opponent can afford to pay for all selected cards
        int totalLifeCost = selectedCardIds.size() * lifeCost;
        int opponentLife = gameData.playerLifeTotals.get(opponentId);
        if (totalLifeCost > opponentLife) {
            throw new IllegalStateException("Not enough life to pay for " + selectedCardIds.size()
                    + " cards (need " + totalLifeCost + ", have " + opponentLife + ")");
        }

        // Separate selected (denied) cards from unselected (to hand)
        Set<UUID> deniedIds = new HashSet<>(selectedCardIds);
        List<Card> toHand = new ArrayList<>();
        List<Card> toExile = new ArrayList<>();
        for (Card card : allRevealedCards) {
            if (deniedIds.contains(card.getId())) {
                toExile.add(card);
            } else {
                toHand.add(card);
            }
        }

        // Opponent pays life for each denied card
        if (!toExile.isEmpty()) {
            gameData.playerLifeTotals.merge(opponentId, -totalLifeCost, Integer::sum);
            gameBroadcastService.logAndBroadcast(gameData,
                    opponentName + " pays " + totalLifeCost + " life to deny "
                            + toExile.stream().map(Card::getName).reduce((a, b) -> a + ", " + b).orElse("") + ".");
        }

        // Cards not denied go to controller's hand
        for (Card card : toHand) {
            gameData.addCardToHand(controllerId, card);
        }
        if (!toHand.isEmpty()) {
            String handNames = toHand.stream().map(Card::getName).reduce((a, b) -> a + ", " + b).orElse("");
            gameBroadcastService.logAndBroadcast(gameData,
                    controllerName + " puts " + handNames + " into their hand.");
        }

        // Denied cards are exiled
        for (Card card : toExile) {
            exileService.exileCard(gameData, controllerId, card);
        }
        if (!toExile.isEmpty()) {
            String exileNames = toExile.stream().map(Card::getName).reduce((a, b) -> a + ", " + b).orElse("");
            gameBroadcastService.logAndBroadcast(gameData,
                    controllerName + " exiles " + exileNames + ".");
        }

        log.info("Game {} - Punisher reveal resolved: {} to hand, {} exiled ({} paid {} life)",
                gameData.id, toHand.size(), toExile.size(), opponentName, totalLifeCost);

        stateBasedActionService.performStateBasedActions(gameData);
        turnProgressionService.resolveAutoPass(gameData);
    }


    /**
     * Handles the player's choice from a Sunbird's Invocation (or similar) "cast without paying"
     * reveal. If a card is chosen, it is cast without paying its mana cost; remaining revealed
     * cards go to the bottom of the library in a random order.
     */
    private void handleCastWithoutPayingChoice(GameData gameData, Player player, int cardIndex,
                                                boolean canFailToFind, List<Card> searchCards,
                                                List<Card> sourceCards, List<Card> deck) {
        UUID playerId = player.getId();
        String playerName = player.getUsername();

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
            // Remove chosen card from sourceCards
            for (int i = 0; i < sourceCards.size(); i++) {
                if (sourceCards.get(i).getId().equals(chosenCard.getId())) {
                    sourceCards.remove(i);
                    break;
                }
            }
        }

        // Put remaining cards on the bottom of the library in a random order
        if (!sourceCards.isEmpty()) {
            Collections.shuffle(sourceCards);
            deck.addAll(sourceCards);
            log.info("Game {} - {} remaining revealed cards shuffled to bottom of library",
                    gameData.id, sourceCards.size());
        }

        if (chosenCard == null) {
            String logEntry = playerName + " declines to cast a spell.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} declines Sunbird's Invocation cast", gameData.id, playerName);
            turnProgressionService.resolveAutoPass(gameData);
            return;
        }

        // Cast the chosen card without paying its mana cost
        StackEntryType spellType = mapCardTypeToSpellType(chosenCard);
        List<CardEffect> spellEffects = new ArrayList<>(chosenCard.getEffects(EffectSlot.SPELL));

        if (EffectResolution.needsTarget(chosenCard)) {
            Set<TargetType> allowedTargets = EffectResolution.computeAllowedTargets(chosenCard);
            List<UUID> validTargets = new ArrayList<>();

            if (allowedTargets.contains(TargetType.PERMANENT)) {
                for (UUID pid : gameData.orderedPlayerIds) {
                    List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
                    if (battlefield == null) continue;
                    for (Permanent p : battlefield) {
                        if (chosenCard.getTargetFilter() instanceof PermanentPredicateTargetFilter filter) {
                            if (predicateEvaluationService.matchesPermanentPredicate(gameData, p, filter.predicate())) {
                                validTargets.add(p.getId());
                            }
                        } else if (gameQueryService.isCreature(gameData, p)) {
                            validTargets.add(p.getId());
                        }
                    }
                }
            }

            if (allowedTargets.contains(TargetType.PLAYER)) {
                validTargets.addAll(gameData.orderedPlayerIds);
            }

            if (validTargets.isEmpty()) {
                // No valid targets — card goes to graveyard
                graveyardService.addCardToGraveyard(gameData, playerId, chosenCard);
                String logEntry = chosenCard.getName() + " has no valid targets and is put into the graveyard.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} cast-without-paying has no valid targets", gameData.id, chosenCard.getName());
                turnProgressionService.resolveAutoPass(gameData);
                return;
            }

            gameData.interaction.setPermanentChoiceContext(
                    new PermanentChoiceContext.LibraryCastSpellTarget(chosenCard, playerId, spellEffects, spellType));
            playerInputService.beginPermanentChoice(gameData, playerId, validTargets,
                    "Choose a target for " + chosenCard.getName() + ".");

            String logEntry = playerName + " casts " + chosenCard.getName()
                    + " without paying its mana cost — choosing target.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} casts {} (Sunbird's Invocation), choosing target",
                    gameData.id, playerName, chosenCard.getName());
            return;
        }

        // Non-targeted spell — put directly on stack
        gameData.stack.add(new StackEntry(
                spellType, chosenCard, playerId, chosenCard.getName(),
                spellEffects, 0, (UUID) null, null
        ));

        gameData.recordSpellCast(playerId, chosenCard);
        gameData.priorityPassedBy.clear();

        String logEntry = playerName + " casts " + chosenCard.getName() + " without paying its mana cost.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} casts {} (Sunbird's Invocation) without paying mana",
                gameData.id, playerName, chosenCard.getName());

        triggerCollectionService.checkSpellCastTriggers(gameData, chosenCard, playerId, false);
        gameBroadcastService.broadcastGameState(gameData);
    }

    private static StackEntryType mapCardTypeToSpellType(Card card) {
        return switch (card.getType()) {
            case CREATURE -> StackEntryType.CREATURE_SPELL;
            case ARTIFACT -> StackEntryType.ARTIFACT_SPELL;
            case ENCHANTMENT -> StackEntryType.ENCHANTMENT_SPELL;
            case PLANESWALKER -> StackEntryType.PLANESWALKER_SPELL;
            case INSTANT -> StackEntryType.INSTANT_SPELL;
            case SORCERY -> StackEntryType.SORCERY_SPELL;
            default -> StackEntryType.SORCERY_SPELL;
        };
    }

}
