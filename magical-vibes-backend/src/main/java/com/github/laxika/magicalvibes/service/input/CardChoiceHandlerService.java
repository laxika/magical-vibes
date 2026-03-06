package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.InteractionContext;
import com.github.laxika.magicalvibes.model.PendingReturnToHandOnDiscardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.service.effect.EffectResolutionService;
import com.github.laxika.magicalvibes.service.effect.PlayerInteractionResolutionService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.PlayerInputService;
import com.github.laxika.magicalvibes.service.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.TurnProgressionService;
import com.github.laxika.magicalvibes.service.ability.AbilityActivationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardChoiceHandlerService {

    private final GameQueryService gameQueryService;
    private final GraveyardService graveyardService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final TriggerCollectionService triggerCollectionService;
    private final TurnProgressionService turnProgressionService;
    private final AbilityActivationService abilityActivationService;
    private final EffectResolutionService effectResolutionService;
    private final PlayerInteractionResolutionService playerInteractionResolutionService;

    public void handleCardChosen(GameData gameData, Player player, int cardIndex) {
        AwaitingInput awaitingInput = gameData.interaction.awaitingInputType();
        if (awaitingInput == AwaitingInput.DISCARD_CHOICE) {
            handleDiscardCardChosen(gameData, player, cardIndex);
            return;
        }
        if (awaitingInput == AwaitingInput.ACTIVATED_ABILITY_DISCARD_COST_CHOICE) {
            abilityActivationService.handleActivatedAbilityDiscardCostChosen(gameData, player, cardIndex);
            return;
        }

        if (awaitingInput == AwaitingInput.REVEALED_HAND_CHOICE) {
            handleRevealedHandCardChosen(gameData, player, cardIndex);
            return;
        }
        if (awaitingInput == AwaitingInput.IMPRINT_FROM_HAND_CHOICE) {
            handleImprintFromHandCardChosen(gameData, player, cardIndex);
            return;
        }

        if (awaitingInput != AwaitingInput.CARD_CHOICE && awaitingInput != AwaitingInput.TARGETED_CARD_CHOICE) {
            throw new IllegalStateException("Not awaiting card choice");
        }
        InteractionContext.CardChoice cardChoice = gameData.interaction.cardChoiceContext();
        if (cardChoice == null || !player.getId().equals(cardChoice.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        UUID playerId = player.getId();
        Set<Integer> validIndices = cardChoice.validIndices();
        boolean isTargeted = awaitingInput == AwaitingInput.TARGETED_CARD_CHOICE;

        gameData.interaction.clearAwaitingInput();
        gameData.interaction.clearCardChoice();

        UUID targetPermanentId = cardChoice.targetPermanentId();

        if (cardIndex == -1) {
            String logEntry = player.getUsername() + " chooses not to put a card onto the battlefield.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} declines to put a card onto the battlefield", gameData.id, player.getUsername());
        } else {
            if (!validIndices.contains(cardIndex)) {
                throw new IllegalStateException("Invalid card index: " + cardIndex);
            }

            List<Card> hand = gameData.playerHands.get(playerId);
            Card card = hand.remove(cardIndex);

            if (isTargeted) {
                resolveTargetedCardChoice(gameData, player, playerId, hand, card, targetPermanentId);
            } else {
                resolveUntargetedCardChoice(gameData, player, playerId, hand, card);
            }
        }

        turnProgressionService.resolveAutoPass(gameData);
    }

    private void handleDiscardCardChosen(GameData gameData, Player player, int cardIndex) {
        InteractionContext.CardChoice cardChoice = gameData.interaction.cardChoiceContext();
        if (cardChoice == null || !player.getId().equals(cardChoice.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        Set<Integer> validIndices = cardChoice.validIndices();
        if (!validIndices.contains(cardIndex)) {
            throw new IllegalStateException("Invalid card index: " + cardIndex);
        }

        UUID playerId = player.getId();
        List<Card> hand = gameData.playerHands.get(playerId);
        Card card = hand.remove(cardIndex);

        graveyardService.addCardToGraveyard(gameData, playerId, card);

        String logEntry = player.getUsername() + " discards " + card.getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} discards {}", gameData.id, player.getUsername(), card.getName());

        triggerCollectionService.checkDiscardTriggers(gameData, playerId, card);

        // Check if a spell should return to hand based on the discarded card type (e.g. Psychic Miasma)
        checkPendingReturnToHandOnDiscard(gameData, card);

        int remainingDiscards = gameData.interaction.decrementDiscardRemainingCount();

        if (remainingDiscards > 0 && !hand.isEmpty()) {
            playerInputService.beginDiscardChoice(gameData, playerId);
        } else {
            gameData.interaction.clearAwaitingInput();
            gameData.interaction.clearCardChoice();
            gameData.interaction.setDiscardRemainingCount(0);
            finalizePendingReturnToHandOnDiscard(gameData);

            // After cleanup discard, apply end-of-turn resets (CR 514.2)
            if (gameData.cleanupDiscardPending) {
                gameData.cleanupDiscardPending = false;
                turnProgressionService.applyCleanupResets(gameData);
            }

            // Continue "each player discards" queue (e.g. Serum Raker's death trigger)
            if (!gameData.pendingEachPlayerDiscardQueue.isEmpty()) {
                playerInteractionResolutionService.startNextEachPlayerDiscard(gameData);
                return;
            }

            // Process any pending self-discard triggers (e.g. Guerrilla Tactics)
            if (!gameData.pendingDiscardSelfTriggers.isEmpty()) {
                triggerCollectionService.processNextDiscardSelfTrigger(gameData);
                return;
            }

            // Resume resolving remaining effects on the same spell/ability
            // (e.g. "Target player discards a card, then mills a card.")
            if (gameData.pendingEffectResolutionEntry != null) {
                effectResolutionService.resolveEffectsFrom(gameData,
                        gameData.pendingEffectResolutionEntry,
                        gameData.pendingEffectResolutionIndex);
            }

            turnProgressionService.resolveAutoPass(gameData);
        }
    }

    private void handleRevealedHandCardChosen(GameData gameData, Player player, int cardIndex) {
        InteractionContext.RevealedHandChoice revealedHandChoice = gameData.interaction.revealedHandChoiceContext();
        if (revealedHandChoice == null || !player.getId().equals(revealedHandChoice.choosingPlayerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        Set<Integer> validIndices = revealedHandChoice.validIndices();
        if (!validIndices.contains(cardIndex)) {
            throw new IllegalStateException("Invalid card index: " + cardIndex);
        }

        UUID targetPlayerId = revealedHandChoice.targetPlayerId();
        List<Card> targetHand = gameData.playerHands.get(targetPlayerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);

        Card chosenCard = targetHand.remove(cardIndex);
        gameData.interaction.addRevealedHandChosenCard(chosenCard);

        String logEntry = player.getUsername() + " chooses " + chosenCard.getName() + " from " + targetName + "'s hand.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} chooses {} from {}'s hand", gameData.id, player.getUsername(), chosenCard.getName(), targetName);

        int remainingChoices = gameData.interaction.decrementRevealedHandChoiceRemainingCount();
        boolean discardMode = gameData.interaction.revealedHandChoiceDiscardMode();

        if (remainingChoices > 0 && !targetHand.isEmpty()) {
            // More cards to choose — update valid indices and prompt again
            List<Integer> newValidIndices = new ArrayList<>();
            for (int i = 0; i < targetHand.size(); i++) {
                newValidIndices.add(i);
            }

            String prompt = discardMode
                    ? "Choose another card to discard."
                    : "Choose another card to put on top of " + targetName + "'s library.";
            playerInputService.beginRevealedHandChoice(gameData, player.getId(), targetPlayerId, newValidIndices, prompt);
        } else {
            // All cards chosen
            gameData.interaction.clearAwaitingInput();
            gameData.interaction.clearCardChoice();

            List<Card> chosenCards = gameData.interaction.revealedHandChosenCardsSnapshot();

            if (discardMode) {
                // Discard chosen cards to graveyard
                for (Card discarded : chosenCards) {
                    graveyardService.addCardToGraveyard(gameData, targetPlayerId, discarded);
                }

                String cardNames = String.join(", ", chosenCards.stream().map(Card::getName).toList());
                String discardLog = targetName + " discards " + cardNames + ".";
                gameBroadcastService.logAndBroadcast(gameData, discardLog);
                log.info("Game {} - {} discards {} from {}'s hand", gameData.id, player.getUsername(), cardNames, targetName);

                for (Card discarded : chosenCards) {
                    triggerCollectionService.checkDiscardTriggers(gameData, targetPlayerId, discarded);
                }
            } else {
                // Put chosen cards on top of library
                List<Card> deck = gameData.playerDecks.get(targetPlayerId);

                // Insert in reverse order so first chosen ends up on top
                for (int i = chosenCards.size() - 1; i >= 0; i--) {
                    deck.addFirst(chosenCards.get(i));
                }

                String cardNames = String.join(", ", chosenCards.stream().map(Card::getName).toList());
                String putLog = player.getUsername() + " puts " + cardNames + " on top of " + targetName + "'s library.";
                gameBroadcastService.logAndBroadcast(gameData, putLog);
                log.info("Game {} - {} puts {} on top of {}'s library", gameData.id, player.getUsername(), cardNames, targetName);
            }

            gameData.interaction.clearRevealedHandChoiceProgress();

            // Process any pending self-discard triggers (e.g. Guerrilla Tactics)
            if (!gameData.pendingDiscardSelfTriggers.isEmpty()) {
                triggerCollectionService.processNextDiscardSelfTrigger(gameData);
                return;
            }

            turnProgressionService.resolveAutoPass(gameData);
        }
    }

    private void handleImprintFromHandCardChosen(GameData gameData, Player player, int cardIndex) {
        InteractionContext.CardChoice cardChoice = gameData.interaction.cardChoiceContext();
        if (cardChoice == null || !player.getId().equals(cardChoice.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        Set<Integer> validIndices = cardChoice.validIndices();
        if (!validIndices.contains(cardIndex)) {
            throw new IllegalStateException("Invalid card index: " + cardIndex);
        }

        UUID playerId = player.getId();
        UUID sourcePermanentId = cardChoice.targetPermanentId();

        gameData.interaction.clearAwaitingInput();
        gameData.interaction.clearCardChoice();

        List<Card> hand = gameData.playerHands.get(playerId);
        Card card = hand.remove(cardIndex);

        // Add to controller's exile zone
        gameData.playerExiledCards.computeIfAbsent(playerId, k -> new ArrayList<>()).add(card);

        // Imprint on source permanent
        Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (sourcePermanent != null) {
            sourcePermanent.getCard().setImprintedCard(card);

            String logEntry = card.getName() + " is exiled and imprinted on " + sourcePermanent.getCard().getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} imprinted {} from hand on {}", gameData.id, player.getUsername(), card.getName(), sourcePermanent.getCard().getName());
        } else {
            String logEntry = card.getName() + " is exiled (source permanent no longer on the battlefield).";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - Source permanent left battlefield, {} exiled without imprinting", gameData.id, card.getName());
        }

        turnProgressionService.resolveAutoPass(gameData);
    }

    private void resolveTargetedCardChoice(GameData gameData, Player player, UUID playerId, List<Card> hand, Card card, UUID targetPermanentId) {
        Permanent target = gameQueryService.findPermanentById(gameData, targetPermanentId);
        if (target != null) {
            Permanent auraPerm = new Permanent(card);
            auraPerm.setAttachedTo(target.getId());
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, playerId, auraPerm);

            String logEntry = player.getUsername() + " puts " + card.getName() + " onto the battlefield attached to " + target.getCard().getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} puts {} onto the battlefield attached to {}", gameData.id, player.getUsername(), card.getName(), target.getCard().getName());
        } else {
            hand.add(card);
            String logEntry = card.getName() + " can't be attached (target left the battlefield).";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - Aura target gone, {} returned to hand", gameData.id, card.getName());
        }
    }

    private void resolveUntargetedCardChoice(GameData gameData, Player player, UUID playerId, List<Card> hand, Card card) {
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, playerId, new Permanent(card));

        String logEntry = player.getUsername() + " puts " + card.getName() + " onto the battlefield.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} puts {} onto the battlefield", gameData.id, player.getUsername(), card.getName());

        battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, playerId, card, null, false);
    }

    private void checkPendingReturnToHandOnDiscard(GameData gameData, Card discardedCard) {
        PendingReturnToHandOnDiscardType pending = gameData.pendingReturnToHandOnDiscardType;
        if (pending == null) {
            return;
        }
        if (discardedCard.getType() == pending.requiredType()
                || discardedCard.getAdditionalTypes().contains(pending.requiredType())) {
            gameData.playerHands.get(pending.controllerId()).add(pending.card());
            String logEntry = pending.card().getName() + " is returned to its owner's hand.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} returned to hand (land discarded)", gameData.id, pending.card().getName());
            gameData.pendingReturnToHandOnDiscardType = null;
        }
    }

    private void finalizePendingReturnToHandOnDiscard(GameData gameData) {
        PendingReturnToHandOnDiscardType pending = gameData.pendingReturnToHandOnDiscardType;
        if (pending == null) {
            return;
        }
        // No matching card type was discarded — spell goes to graveyard as normal
        graveyardService.addCardToGraveyard(gameData, pending.controllerId(), pending.card());
        gameData.pendingReturnToHandOnDiscardType = null;
    }
}


