package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.effect.EnterBattlefieldOnDiscardEffect;
import com.github.laxika.magicalvibes.model.InteractionContext;
import com.github.laxika.magicalvibes.model.PendingExileReturn;
import com.github.laxika.magicalvibes.model.PendingReturnToHandOnDiscardType;
import com.github.laxika.magicalvibes.model.PendingTransformOnCreatureDiscard;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.effect.EffectResolutionService;
import com.github.laxika.magicalvibes.service.effect.PlayerInteractionResolutionService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.turn.TurnProgressionService;
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

    private final DrawService drawService;
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
    private final ExileService exileService;

    public void handleCardChosen(GameData gameData, Player player, int cardIndex) {
        AwaitingInput awaitingInput = gameData.interaction.awaitingInputType();
        if (awaitingInput == AwaitingInput.DISCARD_CHOICE) {
            handleDiscardCardChosen(gameData, player, cardIndex);
            return;
        }
        if (awaitingInput == AwaitingInput.EXILE_FROM_HAND_CHOICE) {
            handleExileFromHandChosen(gameData, player, cardIndex);
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

        UUID targetId = cardChoice.targetId();

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
                resolveTargetedCardChoice(gameData, player, playerId, hand, card, targetId);
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
            // Invalid index (e.g. player clicked "Decline" sending -1) — re-prompt the discard choice
            log.warn("Game {} - {} sent invalid discard card index {}, re-prompting", gameData.id, player.getUsername(), cardIndex);
            playerInputService.beginDiscardChoice(gameData, player.getId());
            return;
        }

        UUID playerId = player.getId();
        List<Card> hand = gameData.playerHands.get(playerId);
        Card card = hand.remove(cardIndex);

        boolean replacedByBattlefield = false;
        if (hasEnterBattlefieldOnDiscardEffect(card) && gameData.discardCausedByOpponent) {
            // Replacement effect: put onto battlefield instead of graveyard (e.g. Obstinate Baloth)
            Permanent permanent = new Permanent(card);
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, playerId, permanent);
            String logEntry = player.getUsername() + " discards " + card.getName() + " — it enters the battlefield instead.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} discards {} — replacement effect puts it onto the battlefield", gameData.id, player.getUsername(), card.getName());
            replacedByBattlefield = true;
        } else {
            graveyardService.addCardToGraveyard(gameData, playerId, card);
            String logEntry = player.getUsername() + " discards " + card.getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} discards {}", gameData.id, player.getUsername(), card.getName());
        }

        triggerCollectionService.checkDiscardTriggers(gameData, playerId, card);

        if (replacedByBattlefield && card.hasType(CardType.CREATURE)) {
            battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, playerId, card, null, false);
        }

        // Check if a spell should return to hand based on the discarded card type (e.g. Psychic Miasma)
        checkPendingReturnToHandOnDiscard(gameData, card);

        // Check if a creature discard should untap + transform the source (e.g. Civilized Scholar)
        checkPendingTransformOnCreatureDiscard(gameData, card);

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

            // Draw cards after "discard up to N, then draw that many" completes
            if (gameData.pendingRummageDrawCount > 0) {
                int drawCount = gameData.pendingRummageDrawCount;
                gameData.pendingRummageDrawCount = 0;
                for (int i = 0; i < drawCount; i++) {
                    drawService.resolveDrawCard(gameData, playerId);
                }
                String drawPlayerName = gameData.playerIdToName.get(playerId);
                gameBroadcastService.logAndBroadcast(gameData,
                        drawPlayerName + " draws " + drawCount + " card" + (drawCount != 1 ? "s" : "") + ".");
            }

            // Untap permanent after "discard a card, then untap [source]" completes
            if (gameData.pendingUntapAfterDiscardPermanentId != null) {
                UUID permanentId = gameData.pendingUntapAfterDiscardPermanentId;
                gameData.pendingUntapAfterDiscardPermanentId = null;
                for (UUID pid : gameData.orderedPlayerIds) {
                    List<Permanent> bf = gameData.playerBattlefields.get(pid);
                    if (bf == null) continue;
                    for (Permanent p : bf) {
                        if (p.getId().equals(permanentId)) {
                            p.untap();
                            String untapLog = p.getCard().getName() + " untaps.";
                            gameBroadcastService.logAndBroadcast(gameData, untapLog);
                            break;
                        }
                    }
                }
            }

            // Resume resolving remaining effects on the same spell/ability
            // (e.g. "Target player discards a card, then mills a card.")
            if (gameData.pendingEffectResolutionEntry != null) {
                effectResolutionService.resolveEffectsFrom(gameData,
                        gameData.pendingEffectResolutionEntry,
                        gameData.pendingEffectResolutionIndex);
            }

            // A resumed effect may have created a pending may ability
            // (e.g. Frightful Delusion: discard → counter-unless-pay)
            if (!gameData.pendingMayAbilities.isEmpty()) {
                playerInputService.processNextMayAbility(gameData);
                return;
            }

            if (gameData.interaction.isAwaitingInput()) {
                return;
            }

            turnProgressionService.resolveAutoPass(gameData);
        }
    }

    private void handleExileFromHandChosen(GameData gameData, Player player, int cardIndex) {
        InteractionContext.CardChoice cardChoice = gameData.interaction.cardChoiceContext();
        if (cardChoice == null || !player.getId().equals(cardChoice.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        Set<Integer> validIndices = cardChoice.validIndices();
        if (!validIndices.contains(cardIndex)) {
            log.warn("Game {} - {} sent invalid exile card index {}, re-prompting", gameData.id, player.getUsername(), cardIndex);
            playerInputService.beginExileFromHandChoice(gameData, player.getId(), cardChoice.targetId());
            return;
        }

        UUID playerId = player.getId();
        UUID sourcePermanentId = cardChoice.targetId();
        List<Card> hand = gameData.playerHands.get(playerId);
        Card card = hand.remove(cardIndex);

        // Add to player's exile zone, tracked with source permanent if available (e.g. Karn Liberated)
        if (sourcePermanentId != null) {
            exileService.exileCard(gameData, playerId, card, sourcePermanentId);
        } else {
            exileService.exileCard(gameData, playerId, card);
        }

        String logEntry = player.getUsername() + " exiles " + card.getName() + " from hand.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} exiles {} from hand", gameData.id, player.getUsername(), card.getName());

        int remainingExiles = gameData.interaction.decrementDiscardRemainingCount();

        if (remainingExiles > 0 && !hand.isEmpty()) {
            playerInputService.beginExileFromHandChoice(gameData, playerId, sourcePermanentId);
        } else {
            gameData.interaction.clearAwaitingInput();
            gameData.interaction.clearCardChoice();
            gameData.interaction.setDiscardRemainingCount(0);

            // Resume resolving remaining effects
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
        boolean discardMode = gameData.interaction.revealedHandChoice().discardMode();
        boolean exileMode = gameData.interaction.revealedHandChoice().exileMode();

        if (remainingChoices > 0 && !targetHand.isEmpty()) {
            // More cards to choose — update valid indices and prompt again
            List<Integer> newValidIndices = new ArrayList<>();
            for (int i = 0; i < targetHand.size(); i++) {
                newValidIndices.add(i);
            }

            String prompt;
            if (discardMode) {
                prompt = "Choose another card to discard.";
            } else if (exileMode) {
                prompt = "Choose another card to exile.";
            } else {
                prompt = "Choose another card to put on top of " + targetName + "'s library.";
            }
            playerInputService.beginRevealedHandChoice(gameData, player.getId(), targetPlayerId, newValidIndices, prompt);
        } else {
            // All cards chosen
            gameData.interaction.clearAwaitingInput();
            gameData.interaction.clearCardChoice();

            List<Card> chosenCards = gameData.interaction.revealedHandChoice().chosenCardsSnapshot();

            if (discardMode) {
                // Discard chosen cards to graveyard (or battlefield if replacement effect applies)
                List<Card> replacedCards = new ArrayList<>();
                for (Card discarded : chosenCards) {
                    if (hasEnterBattlefieldOnDiscardEffect(discarded) && gameData.discardCausedByOpponent) {
                        Permanent permanent = new Permanent(discarded);
                        battlefieldEntryService.putPermanentOntoBattlefield(gameData, targetPlayerId, permanent);
                        replacedCards.add(discarded);
                        String replaceLog = targetName + " discards " + discarded.getName() + " — it enters the battlefield instead.";
                        gameBroadcastService.logAndBroadcast(gameData, replaceLog);
                        log.info("Game {} - {} discards {} — replacement effect puts it onto the battlefield",
                                gameData.id, targetName, discarded.getName());
                    } else {
                        graveyardService.addCardToGraveyard(gameData, targetPlayerId, discarded);
                    }
                }

                List<Card> normallyDiscarded = chosenCards.stream()
                        .filter(c -> !replacedCards.contains(c))
                        .toList();
                if (!normallyDiscarded.isEmpty()) {
                    String cardNames = String.join(", ", normallyDiscarded.stream().map(Card::getName).toList());
                    String discardLog = targetName + " discards " + cardNames + ".";
                    gameBroadcastService.logAndBroadcast(gameData, discardLog);
                    log.info("Game {} - {} discards {} from {}'s hand", gameData.id, player.getUsername(), cardNames, targetName);
                }

                for (Card discarded : chosenCards) {
                    triggerCollectionService.checkDiscardTriggers(gameData, targetPlayerId, discarded);
                }

                for (Card replaced : replacedCards) {
                    if (replaced.hasType(CardType.CREATURE)) {
                        battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, targetPlayerId, replaced, null, false);
                    }
                }
            } else if (exileMode) {
                // Exile chosen cards
                for (Card exiled : chosenCards) {
                    exileService.exileCard(gameData, targetPlayerId, exiled);
                }

                String cardNames = String.join(", ", chosenCards.stream().map(Card::getName).toList());
                String exileLog = player.getUsername() + " exiles " + cardNames + " from " + targetName + "'s hand.";
                gameBroadcastService.logAndBroadcast(gameData, exileLog);
                log.info("Game {} - {} exiles {} from {}'s hand", gameData.id, player.getUsername(), cardNames, targetName);

                // Track return-on-source-leave for exile-until-leaves effects (e.g. Kitesail Freebooter)
                UUID sourcePermanentId = gameData.interaction.revealedHandChoice().sourcePermanentId();
                if (sourcePermanentId != null) {
                    for (Card exiled : chosenCards) {
                        gameData.exileReturnOnPermanentLeave.put(sourcePermanentId,
                                new PendingExileReturn(exiled, targetPlayerId, false, true));
                    }
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
            log.warn("Game {} - {} sent invalid imprint card index {}, re-prompting", gameData.id, player.getUsername(), cardIndex);
            playerInputService.beginImprintFromHandChoice(gameData, player.getId(),
                    new ArrayList<>(validIndices), "Choose a card from your hand.", cardChoice.targetId());
            return;
        }

        UUID playerId = player.getId();
        UUID sourcePermanentId = cardChoice.targetId();

        gameData.interaction.clearAwaitingInput();
        gameData.interaction.clearCardChoice();

        List<Card> hand = gameData.playerHands.get(playerId);
        Card card = hand.remove(cardIndex);

        // Add to controller's exile zone
        exileService.exileCard(gameData, playerId, card);

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

    private void resolveTargetedCardChoice(GameData gameData, Player player, UUID playerId, List<Card> hand, Card card, UUID targetId) {
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target != null) {
            Permanent auraPerm = new Permanent(card);
            auraPerm.setAttachedTo(target.getId());
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, playerId, auraPerm);

            String logEntry = player.getUsername() + " puts " + card.getName() + " onto the battlefield attached to " + target.getCard().getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} puts {} onto the battlefield attached to {}", gameData.id, player.getUsername(), card.getName(), target.getCard().getName());
        } else {
            gameData.addCardToHand(playerId, card);
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
        if (discardedCard.hasType(pending.requiredType())) {
            gameData.addCardToHand(pending.controllerId(), pending.card());
            String logEntry = pending.card().getName() + " is returned to its owner's hand.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} returned to hand (land discarded)", gameData.id, pending.card().getName());
            gameData.pendingReturnToHandOnDiscardType = null;
        }
    }

    private void checkPendingTransformOnCreatureDiscard(GameData gameData, Card discardedCard) {
        PendingTransformOnCreatureDiscard pending = gameData.pendingTransformOnCreatureDiscard;
        if (pending == null) {
            return;
        }
        gameData.pendingTransformOnCreatureDiscard = null;
        if (discardedCard.hasType(CardType.CREATURE)) {
            Permanent source = gameQueryService.findPermanentById(gameData, pending.sourcePermanentId());
            if (source == null) {
                return;
            }
            // Untap
            source.untap();
            String untapLog = source.getCard().getName() + " untaps.";
            gameBroadcastService.logAndBroadcast(gameData, untapLog);
            log.info("Game {} - {} untaps (creature discarded)", gameData.id, source.getCard().getName());

            // Transform
            Card originalCard = source.getOriginalCard();
            Card backFace = originalCard.getBackFaceCard();
            if (backFace != null && !source.isTransformed()) {
                String frontName = source.getCard().getName();
                source.setCard(backFace);
                source.setTransformed(true);
                String transformLog = frontName + " transforms into " + backFace.getName() + ".";
                gameBroadcastService.logAndBroadcast(gameData, transformLog);
                log.info("Game {} - {} transforms into {}", gameData.id, frontName, backFace.getName());
            }
        }
    }

    private boolean hasEnterBattlefieldOnDiscardEffect(Card card) {
        return card.getEffects(EffectSlot.ON_SELF_DISCARDED_BY_OPPONENT).stream()
                .anyMatch(e -> e instanceof EnterBattlefieldOnDiscardEffect);
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


