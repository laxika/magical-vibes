package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingGraveyardReturnChoice;
import com.github.laxika.magicalvibes.model.PendingBoostSourceByDiscardedManaValue;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.PlaguecrafterState;
import com.github.laxika.magicalvibes.model.effect.EnterBattlefieldOnDiscardEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.HandChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.model.action.ReturnExiledCardToHandAtNextEndStep;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingReturnToHandOnDiscardType;
import com.github.laxika.magicalvibes.model.PendingTransformOnCreatureDiscard;
import com.github.laxika.magicalvibes.model.PendingUntapOnDiscardType;
import com.github.laxika.magicalvibes.model.PendingValkiHandExileChoice;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.TargetSpec;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.effect.EffectResolutionService;
import com.github.laxika.magicalvibes.service.effect.GraveyardTargetingSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.EquipSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.GraveyardReturnSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.PermanentCounterSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.PlayerInteractionSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.TapUntapSupport;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import com.github.laxika.magicalvibes.service.target.TargetPredicateEvaluationService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.turn.TurnProgressionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final TriggerCollectionService triggerCollectionService;
    private final TurnProgressionService turnProgressionService;
    private final EffectResolutionService effectResolutionService;
    private final InputCompletionService inputCompletionService;
    private final PlayerInteractionSupport playerInteractionSupport;
    private final TapUntapSupport tapUntapSupport;
    private final PermanentCounterSupport permanentCounterSupport;
    private final LifeSupport lifeSupport;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final EquipSupport equipSupport;
    private final ExileSupport exileSupport;
    private final ExileService exileService;
    private final com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService permanentRemovalService;
    private final com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry interactionHandlerRegistry;
    private final PredicateEvaluationService predicateEvaluationService;
    private final TargetPredicateEvaluationService targetPredicateEvaluationService;
    private final GraveyardTargetingSupport graveyardTargetingSupport;

    /** Answers CARD_CHOICE and TARGETED_CARD_CHOICE (put a card/Aura from hand onto the battlefield). */
    public void handleHandCardChosen(GameData gameData, Player player, int cardIndex) {
        PendingInteraction active = gameData.interaction.activeInteraction();
        UUID choicePlayerId;
        List<Integer> validIndices;
        UUID targetId;
        boolean isTargeted;
        boolean enterTapped = false;
        boolean grantHaste = false;
        boolean sacrificeAtEndStep = false;
        boolean returnToHandAtEndStep = false;
        boolean enterAttacking = false;
        boolean drawAndRepeat = false;
        boolean putAnyNumber = false;
        boolean faceDown = false;
        int faceDownPower = 0;
        int faceDownToughness = 0;
        Set<CardType> faceDownCardTypes = Set.of();
        UUID returnExiledSourceCardId = null;
        UUID returnSourcePermanentId = null;
        CardPredicate drawAndRepeatPredicate = null;
        CardPredicate enterTappedAndAttackingIf = null;
        String drawAndRepeatLabel = null;
        UUID attachEquipmentCardId = null;
        UUID exileSourceIfDeclinedId = null;
        Integer sacrificeUnlessPayGenericReduction = null;
        CounterType artifactCounterType = null;
        int artifactCounterCount = 0;
        if (active instanceof PendingInteraction.HandCardChoice hc) {
            choicePlayerId = hc.playerId();
            validIndices = hc.validIndices();
            targetId = null;
            isTargeted = false;
            enterTapped = hc.enterTapped();
            grantHaste = hc.grantHaste();
            sacrificeAtEndStep = hc.sacrificeAtEndStep();
            returnToHandAtEndStep = hc.returnToHandAtEndStep();
            attachEquipmentCardId = hc.attachEquipmentCardId();
            enterAttacking = hc.enterAttacking();
            sacrificeUnlessPayGenericReduction = hc.sacrificeUnlessPayGenericReduction();
            drawAndRepeat = hc.drawAndRepeat();
            putAnyNumber = hc.putAnyNumber();
            returnExiledSourceCardId = hc.returnExiledSourceCardId();
            drawAndRepeatPredicate = hc.drawAndRepeatPredicate();
            drawAndRepeatLabel = hc.drawAndRepeatLabel();
            enterTappedAndAttackingIf = hc.enterTappedAndAttackingIf();
            faceDown = hc.faceDown();
            faceDownPower = hc.faceDownPower();
            faceDownToughness = hc.faceDownToughness();
            faceDownCardTypes = hc.faceDownCardTypes();
            returnSourcePermanentId = hc.returnSourcePermanentId();
            artifactCounterType = hc.artifactCounterType();
            artifactCounterCount = hc.artifactCounterCount();
        } else if (active instanceof PendingInteraction.TargetedHandCardChoice thc) {
            choicePlayerId = thc.playerId();
            validIndices = thc.validIndices();
            targetId = thc.targetId();
            isTargeted = true;
            exileSourceIfDeclinedId = thc.exileSourceIfDeclinedId();
        } else {
            throw new IllegalStateException("Not your turn to choose");
        }
        if (!player.getId().equals(choicePlayerId)) {
            throw new IllegalStateException("Not your turn to choose");
        }

        UUID playerId = player.getId();

        // Validate before touching interaction state: a rejected answer must leave the prompt
        // standing so the player can answer again. Clearing first and then throwing destroys the
        // only thing that would resume the entry parked in pendingEffectResolutionEntry, wedging
        // the game (and with it deferPlayerLossCheck) on a stale client answer. The copy below
        // stays as defence.
        if (cardIndex != -1 && !validIndices.contains(cardIndex)) {
            throw new IllegalStateException("Invalid card index: " + cardIndex);
        }

        gameData.interaction.clearAwaitingInput();

        if (cardIndex == -1) {
            String logEntry = player.getUsername() + " chooses not to put a card onto the battlefield.";
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} declines to put a card onto the battlefield", gameData.id, player.getUsername());

            // "If you don't, exile this creature." (Evershrike) — declining exiles the source permanent.
            if (exileSourceIfDeclinedId != null) {
                Permanent source = gameQueryService.findPermanentById(gameData, exileSourceIfDeclinedId);
                if (source != null) {
                    permanentRemovalService.removePermanentToExile(gameData, source);
                    gameLogService.append(gameData, GameLog.isExiled(source.getCard()));
                    log.info("Game {} - {} is exiled (no Aura put onto it)", gameData.id, source.getCard().getName());
                }
            }
        } else {
            if (!validIndices.contains(cardIndex)) {
                throw new IllegalStateException("Invalid card index: " + cardIndex);
            }

            List<Card> hand = gameData.playerHands.get(playerId);
            Card card = hand.remove(cardIndex);

            if (isTargeted) {
                resolveTargetedCardChoice(gameData, player, playerId, card, targetId);
            } else {
                UUID sourceCardId = gameData.pendingEffectResolutionEntry == null
                        || gameData.pendingEffectResolutionEntry.getCard() == null
                        ? null : gameData.pendingEffectResolutionEntry.getCard().getId();
                boolean enterTappedAndAttacking = enterTappedAndAttackingIf != null
                        && predicateEvaluationService.matchesCardPredicate(card, enterTappedAndAttackingIf,
                        sourceCardId, gameData, playerId);
                boolean selectedEnterTapped = enterTapped || enterTappedAndAttacking;
                boolean selectedEnterAttacking = enterAttacking || enterTappedAndAttacking;
                Permanent enteredPermanent = resolveUntargetedCardChoice(gameData, player, playerId, card, selectedEnterTapped, grantHaste,
                        sacrificeAtEndStep, returnToHandAtEndStep, attachEquipmentCardId, selectedEnterAttacking, sacrificeUnlessPayGenericReduction,
                        faceDown, faceDownPower, faceDownToughness, faceDownCardTypes,
                        returnExiledSourceCardId);
                if (artifactCounterType != null && gameQueryService.isArtifact(gameData, enteredPermanent)) {
                    permanentCounterSupport.placeCounterOnPermanent(gameData,
                            gameData.pendingEffectResolutionEntry, enteredPermanent,
                            artifactCounterType, artifactCounterCount);
                }
                if (returnSourcePermanentId != null) {
                    StackEntry pendingEntry = gameData.pendingEffectResolutionEntry;
                    if (pendingEntry != null) {
                        pendingEntry.insertEffectsToResolve(gameData.pendingEffectResolutionIndex,
                                List.of(ReturnToHandEffect.self()));
                    }
                }
                // Cultivator Colossus / Wrenn and Seven: re-offer until decline / no matches.
                if ((drawAndRepeat || putAnyNumber) && drawAndRepeatPredicate != null && drawAndRepeatLabel != null
                        && !gameData.interaction.isAwaitingInput()) {
                    if (drawAndRepeat) {
                        drawService.resolveDrawCard(gameData, playerId);
                    }
                    PutCardToBattlefieldEffect repeatEffect = new PutCardToBattlefieldEffect(drawAndRepeatPredicate,
                            drawAndRepeatLabel, enterTapped, false, false, false, false, false, drawAndRepeat,
                            putAnyNumber, faceDown, faceDownPower, faceDownToughness, faceDownCardTypes);
                    if (enterTappedAndAttackingIf != null) {
                        repeatEffect = repeatEffect.withEnterTappedAndAttackingIf(enterTappedAndAttackingIf);
                    }
                    if (returnToHandAtEndStep) {
                        repeatEffect = repeatEffect.returningToHandAtEndStep();
                    }
                    playerInteractionSupport.applyPutCardToBattlefield(gameData, playerId, repeatEffect);
                }
            }
        }

        // A pay-or-sacrifice may ability may now be awaiting the player's decision (e.g. Flash).
        if (gameData.interaction.isAwaitingInput()) {
            return;
        }

        inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    /** Answers DISCARD_CHOICE, including the multi-pick countdown carried on the record. */
    public void handleDiscardCardChosen(GameData gameData, Player player, int cardIndex) {
        PendingInteraction.DiscardChoice discardChoice =
                gameData.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        if (discardChoice == null || !player.getId().equals(discardChoice.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        if (discardChoice.followUp().plaguecrafter()) {
            handlePlaguecrafterDiscardCardChosen(gameData, player, cardIndex, discardChoice);
            return;
        }

        List<Integer> validIndices = discardChoice.validIndices();
        if (cardIndex == -1 && discardChoice.declinable()) {
            DiscardFollowUp followUp = discardChoice.followUp();
            if (followUp.enteringPermanent() != null) {
                gameData.interaction.clearAwaitingInput();
                battlefieldEntryService.completeDiscardCardToEnter(
                        gameData, followUp.enteringControllerId(), followUp.enteringPermanent(), false);
                resumeRemainingEffectsAfterDiscard(gameData);
            } else {
                finishDiscardChoice(gameData, player, player.getId(), followUp, null);
            }
            return;
        }
        if (!validIndices.contains(cardIndex)) {
            // Invalid index (e.g. player clicked "Decline" sending -1) — re-prompt the discard choice
            // Preserve filtered validIndices/prompt (e.g. DiscardCardThenEffect land-only discard).
            log.warn("Game {} - {} sent invalid discard card index {}, re-prompting", gameData.id, player.getUsername(), cardIndex);
            playerInputService.beginDiscardChoice(gameData, player.getId(), discardChoice.validIndices(),
                    discardChoice.prompt(), discardChoice.remainingCount(), discardChoice.followUp(),
                    discardChoice.stopAfterDiscardingType(), discardChoice.declinable());
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
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " discards ", card, " — it enters the battlefield instead."));
            log.info("Game {} - {} discards {} — replacement effect puts it onto the battlefield", gameData.id, player.getUsername(), card.getName());
            replacedByBattlefield = true;
        } else {
            graveyardService.discardCard(gameData, playerId, card);
            gameLogService.append(gameData, GameLog.playerDiscards(player.getUsername(), card));
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

        // Check if a card type discard should untap the source (e.g. Lumengrid Augur)
        checkPendingUntapOnDiscardType(gameData, card);

        // Check if the discarded card should pump the source by its mana value (e.g. Spellbound Dragon)
        checkPendingBoostSourceByDiscardedManaValue(gameData, card);

        DiscardFollowUp followUp = discardChoice.followUp();
        if (followUp.enteringPermanent() != null) {
            gameData.interaction.clearAwaitingInput();
            battlefieldEntryService.completeDiscardCardToEnter(
                    gameData, followUp.enteringControllerId(), followUp.enteringPermanent(), true);
            if (gameData.interaction.isAwaitingInput()) {
                return;
            }
        }

        int remainingDiscards = Math.max(discardChoice.remainingCount() - 1, 0);
        boolean mayDecline = remainingDiscards > 0
                && discardChoice.stopAfterDiscardingType() != null
                && card.hasType(discardChoice.stopAfterDiscardingType());

        List<Integer> remainingValidIndices = validIndices.stream()
                .filter(index -> index != cardIndex)
                .map(index -> index > cardIndex ? index - 1 : index)
                .toList();

        if (remainingDiscards > 0 && !hand.isEmpty() && !remainingValidIndices.isEmpty()) {
            inputCompletionService.publishStateAfterInput(gameData);
            playerInputService.beginDiscardChoice(gameData, playerId, remainingValidIndices,
                    discardChoice.prompt(), remainingDiscards, discardChoice.followUp(),
                    discardChoice.stopAfterDiscardingType(), mayDecline);
        } else {
            finishDiscardChoice(gameData, player, playerId, discardChoice.followUp(), card);
        }
    }

    private void handlePlaguecrafterDiscardCardChosen(GameData gameData, Player player, int cardIndex,
            PendingInteraction.DiscardChoice discardChoice) {
        if (!discardChoice.validIndices().contains(cardIndex)) {
            throw new IllegalStateException("Invalid discard card index: " + cardIndex);
        }

        List<Card> hand = gameData.playerHands.getOrDefault(player.getId(), List.of());
        if (cardIndex < 0 || cardIndex >= hand.size()) {
            throw new IllegalStateException("Invalid discard card index: " + cardIndex);
        }

        gameData.interaction.clearAwaitingInput();
        gameData.plaguecrafter.selectedDiscards.add(new PlaguecrafterState.SelectedDiscard(
                player.getId(), hand.get(cardIndex).getId()));

        List<UUID> remainingPlayers = discardChoice.followUp().remainingEachPlayerDiscards();
        if (!remainingPlayers.isEmpty()) {
            gameData.rerunCurrentEffectAfterInteraction = true;
            UUID nextPlayerId = remainingPlayers.getFirst();
            playerInputService.beginDiscardChoice(gameData, nextPlayerId, 1,
                    DiscardFollowUp.plaguecrafter(remainingPlayers.subList(1, remainingPlayers.size())));
            return;
        }

        discardCollectedPlaguecrafterCards(gameData);
        gameData.plaguecrafter.completed = true;
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    private void discardCollectedPlaguecrafterCards(GameData gameData) {
        PlaguecrafterState state = gameData.plaguecrafter;
        List<PlaguecrafterState.SelectedDiscard> selected = List.copyOf(state.selectedDiscards);
        List<PlaguecrafterState.SelectedDiscard> actualDiscards = new ArrayList<>();
        Map<UUID, Card> cardsById = new HashMap<>();

        for (PlaguecrafterState.SelectedDiscard selection : selected) {
            List<Card> hand = gameData.playerHands.get(selection.playerId());
            if (hand == null) {
                continue;
            }
            int index = -1;
            for (int i = 0; i < hand.size(); i++) {
                if (hand.get(i).getId().equals(selection.cardId())) {
                    index = i;
                    break;
                }
            }
            if (index >= 0) {
                Card card = hand.remove(index);
                cardsById.put(card.getId(), card);
                actualDiscards.add(selection);
            }
        }

        for (PlaguecrafterState.SelectedDiscard selection : actualDiscards) {
            Card card = cardsById.get(selection.cardId());
            if (card == null) {
                continue;
            }

            boolean causedByOpponent = !selection.playerId().equals(state.sourceControllerId);
            gameData.discardCausedByOpponent = causedByOpponent;
            boolean replacedByBattlefield = false;
            if (hasEnterBattlefieldOnDiscardEffect(card) && causedByOpponent) {
                Permanent permanent = new Permanent(card);
                battlefieldEntryService.putPermanentOntoBattlefield(gameData, selection.playerId(), permanent);
                gameLogService.append(gameData, GameLog.textCardText(
                        gameData.playerIdToName.get(selection.playerId()) + " discards ", card,
                        " — it enters the battlefield instead."));
                replacedByBattlefield = true;
            } else {
                graveyardService.discardCard(gameData, selection.playerId(), card);
                gameLogService.append(gameData, GameLog.playerDiscards(
                        gameData.playerIdToName.get(selection.playerId()), card));
            }

            triggerCollectionService.checkDiscardTriggers(gameData, selection.playerId(), card);
            if (replacedByBattlefield && card.hasType(CardType.CREATURE)) {
                battlefieldEntryService.handleCreatureEnteredBattlefield(
                        gameData, selection.playerId(), card, null, false);
            }
            checkPendingReturnToHandOnDiscard(gameData, card);
            checkPendingTransformOnCreatureDiscard(gameData, card);
            checkPendingUntapOnDiscardType(gameData, card);
            checkPendingBoostSourceByDiscardedManaValue(gameData, card);
        }
    }

    private void finishDiscardChoice(GameData gameData, Player player, UUID playerId,
                                    DiscardFollowUp followUp, Card discardedCard) {
        gameData.interaction.clearAwaitingInput();
        triggerCollectionService.finishDiscardEvent(gameData);
        finalizePendingReturnToHandOnDiscard(gameData);

        // After cleanup discard, apply end-of-turn resets (CR 514.2)
        if (gameData.cleanupDiscardPending) {
            gameData.cleanupDiscardPending = false;
            turnProgressionService.applyCleanupResets(gameData);
        }

        // Continue "each player discards" queue (e.g. Serum Raker's death trigger)
        if (!followUp.remainingEachPlayerDiscards().isEmpty()) {
            followUp = playerInteractionSupport.startNextEachPlayerDiscard(gameData, followUp);
            // The queue can drain without prompting anyone (every remaining player has an
            // empty hand); fall through so the rest of the spell still resolves.
            if (gameData.interaction.isAwaitingInput()) {
                return;
            }
        }

        // Process any pending self-discard triggers (e.g. Guerrilla Tactics)
        if (gameData.hasPendingInteraction(PermanentChoiceContext.DiscardTriggerAnyTarget.class)) {
            triggerCollectionService.processNextDiscardSelfTrigger(gameData);
            return;
        }

        // Draw cards after "discard up to N, then draw that many" completes
        if (followUp.rummageDrawCount() > 0) {
            int drawCount = followUp.rummageDrawCount();
            UUID drawPlayerId = followUp.rummageDrawPlayerId() == null
                    ? playerId : followUp.rummageDrawPlayerId();
            for (int i = 0; i < drawCount; i++) {
                drawService.resolveDrawCard(gameData, drawPlayerId);
            }
            String drawPlayerName = gameData.playerIdToName.get(drawPlayerId);
            gameLogService.append(gameData, GameLog.text(drawPlayerName + " draws " + drawCount + " card" + (drawCount != 1 ? "s" : "") + "."));
        }

        // Untap permanent after "discard a card, then untap [source]" completes
        if (followUp.untapPermanentId() != null) {
            UUID permanentId = followUp.untapPermanentId();
            for (UUID pid : gameData.orderedPlayerIds) {
                List<Permanent> bf = gameData.playerBattlefields.get(pid);
                if (bf == null) continue;
                for (Permanent p : bf) {
                    if (p.getId().equals(permanentId)) {
                        p.untap();
                        gameLogService.append(gameData, GameLog.cardThen(p.getCard(), " untaps."));
                        break;
                    }
                }
            }
        }

        // Boost permanent after "discard a card, then this creature gets +X/+Y" completes
        if (followUp.boostPermanentId() != null) {
            UUID permanentId = followUp.boostPermanentId();
            int powerBoost = followUp.boostPower();
            int toughnessBoost = followUp.boostToughness();
            for (UUID pid : gameData.orderedPlayerIds) {
                List<Permanent> bf = gameData.playerBattlefields.get(pid);
                if (bf == null) continue;
                for (Permanent p : bf) {
                    if (p.getId().equals(permanentId)) {
                        p.setPowerModifier(p.getPowerModifier() + powerBoost);
                        p.setToughnessModifier(p.getToughnessModifier() + toughnessBoost);
                        gameLogService.append(gameData, GameLog.builder()
                                .card(p.getCard())
                                .text(String.format(" gets %+d/%+d until end of turn.",
                                        powerBoost, toughnessBoost))
                                .build());
                        break;
                    }
                }
            }
        }

        if (followUp.plusOnePlusOneCounterPermanentId() != null) {
            Permanent source = gameQueryService.findPermanentById(gameData,
                    followUp.plusOnePlusOneCounterPermanentId());
            if (source != null) {
                permanentCounterSupport.applyPlusOnePlusOneCounters(gameData, null, source,
                        followUp.plusOnePlusOneCounterAmount());
            }
        }

        // Return cards from graveyard to hand after "discard X cards, then return a card for
        // each discarded" completes (Recall). One sequential pick per discarded card; the
        // graveyard holds at least that many (the cards just discarded), so the choice always
        // begins. Once the queue empties, GraveyardChoiceHandlerService resumes the remaining
        // effects (e.g. the trailing ExileSpellEffect).
        if (followUp.graveyardReturnCount() > 0) {
            for (int i = 0; i < followUp.graveyardReturnCount(); i++) {
                gameData.pendingGraveyardReturnQueue.add(new PendingGraveyardReturnChoice(
                        playerId, 1, null, GraveyardChoiceDestination.HAND, false));
            }
            graveyardReturnSupport.beginNextGraveyardReturnFromQueue(gameData);
            if (gameData.interaction.isAwaitingInput()) {
                return;
            }
        }

        // Push "if you do" rider after a filtered discard (DiscardCardThenEffect / Pack Guardian)
        CardEffect selectedThenEffect = followUp.thenEffect();
        if (discardedCard != null && followUp.thenEffectAlternateCardType() != null
                && discardedCard.hasType(followUp.thenEffectAlternateCardType())) {
            selectedThenEffect = followUp.thenEffectAlternate();
        }
        boolean thenEffectConditionMet = followUp.thenEffectCondition() == null
                || discardedCard != null && predicateEvaluationService.matchesCardPredicate(
                        discardedCard, followUp.thenEffectCondition(), followUp.thenEffectSourceCard().getId());
        if (thenEffectConditionMet && selectedThenEffect != null && followUp.thenEffectSourceCard() != null) {
            CardEffect thenEffect = selectedThenEffect;
            Card sourceCard = followUp.thenEffectSourceCard();
            TargetSpec targetSpec = thenEffect.targetSpec();
            boolean hasPreboundTarget = followUp.thenEffectTargetId() != null;
            GraveyardTargetingSupport.Target graveyardTarget = graveyardTargetingSupport.findTarget(List.of(thenEffect));
            if (!hasPreboundTarget && graveyardTarget != null) {
                List<Card> matchingCards = new ArrayList<>();
                for (UUID graveyardOwnerId : graveyardTarget.scope().graveyardOwners(
                        gameData.orderedPlayerIds, playerId)) {
                    List<Card> graveyard = gameData.playerGraveyards.get(graveyardOwnerId);
                    if (graveyard == null) {
                        continue;
                    }
                    for (Card card : graveyard) {
                        if (predicateEvaluationService.matchesCardPredicate(
                                card, graveyardTarget.filter(), sourceCard.getId())) {
                            matchingCards.add(card);
                        }
                    }
                }
                if (!matchingCards.isEmpty() || graveyardTarget.minTargets() == 0) {
                    gameData.graveyardTargetOperation.card = sourceCard;
                    gameData.graveyardTargetOperation.controllerId = playerId;
                    gameData.graveyardTargetOperation.effects = List.of(thenEffect);
                    gameData.graveyardTargetOperation.entryType = null;
                    gameData.graveyardTargetOperation.xValue = 0;
                    gameData.graveyardTargetOperation.anyNumber = false;
                    gameData.graveyardTargetOperation.singleGraveyard = false;
                    gameData.graveyardTargetOperation.targetPlayerId = null;
                    gameData.graveyardTargetOperation.flashback = false;
                    gameData.graveyardTargetOperation.sourcePermanentId = null;
                    gameData.graveyardTargetOperation.chapterName = null;
                    gameData.graveyardTargetOperation.permanentTargetIds = null;

                    String zoneLabel = switch (graveyardTarget.scope()) {
                        case CONTROLLERS_GRAVEYARD -> "your graveyard";
                        case OPPONENT_GRAVEYARD -> "an opponent's graveyard";
                        case ALL_GRAVEYARDS -> "a graveyard";
                    };
                    playerInputService.beginMultiGraveyardChoice(gameData, playerId, matchingCards,
                            graveyardTarget.maxTargets(), graveyardTarget.minTargets(),
                            sourceCard.getName() + " — Choose target card from " + zoneLabel + " "
                                    + graveyardTarget.destination() + ".");
                    return;
                }
            }
            boolean needsTarget = !hasPreboundTarget && (targetSpec.admits(TargetPredicate.Kind.PERMANENT)
                    || targetSpec.admits(TargetPredicate.Kind.PLAYER));
            if (needsTarget) {
                List<UUID> validPermanentTargets = new ArrayList<>();
                int thenEffectXValue = followUp.thenEffectUsesDiscardedManaValue() && discardedCard != null
                        ? discardedCard.getManaValue() : 0;
                if (targetSpec.admits(TargetPredicate.Kind.PERMANENT)) {
                    FilterContext filterContext = FilterContext.of(gameData)
                            .withSourceCardId(sourceCard.getId())
                            .withSourceControllerId(playerId);
                    if (followUp.thenEffectUsesDiscardedManaValue() && discardedCard != null) {
                        filterContext = filterContext.withXValue(thenEffectXValue);
                    }
                    TargetPredicate targetPredicate = targetSpec.targetPredicate();
                    for (UUID targetPlayerId : gameData.orderedPlayerIds) {
                        List<Permanent> battlefield = gameData.playerBattlefields.get(targetPlayerId);
                        if (battlefield == null) {
                            continue;
                        }
                        for (Permanent permanent : battlefield) {
                            if (targetPredicateEvaluationService.matchesPermanent(
                                    targetPredicate, permanent, filterContext)) {
                                validPermanentTargets.add(permanent.getId());
                            }
                        }
                    }
                }
                List<UUID> validPlayerTargets = targetSpec.admits(TargetPredicate.Kind.PLAYER)
                        ? gameData.orderedPlayerIds.stream()
                                .filter(targetId -> targetPredicateEvaluationService.matchesPlayer(
                                        targetSpec.targetPredicate(), targetId, playerId, gameData))
                                .toList()
                        : List.of();
                if (validPermanentTargets.isEmpty() && validPlayerTargets.isEmpty()) {
                    gameLogService.append(gameData, GameLog.cardThen(sourceCard,
                            "'s ability has no valid targets."));
                } else {
                    gameData.interaction.setPermanentChoiceContext(
                            new PermanentChoiceContext.MayAbilityTriggerTarget(
                                    sourceCard, playerId, List.of(thenEffect), null, null, 0,
                                    thenEffectXValue));
                    playerInputService.beginAnyTargetChoice(gameData, playerId,
                            validPermanentTargets, validPlayerTargets,
                            sourceCard.getName() + " — Choose a target for the reflexive trigger.");
                    log.info("Game {} - {} discard-then rider awaiting target for {}",
                            gameData.id, player.getUsername(), sourceCard.getName());
                    return;
                }
            } else if (hasPreboundTarget) {
                StackEntry thenEntry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        sourceCard,
                        playerId,
                        sourceCard.getName() + "'s effect",
                        List.of(thenEffect),
                        followUp.thenEffectTargetId(),
                        (UUID) null);
                thenEntry.setNonTargeting(true);
                gameData.stack.add(thenEntry);
            } else {
                StackEntry reflexiveEntry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        sourceCard,
                        playerId,
                        sourceCard.getName() + "'s effect",
                        List.of(thenEffect)
                );
                reflexiveEntry.setEventValue(followUp.eachPlayerNoDiscardCount());
                gameData.stack.add(reflexiveEntry);
            }
            log.info("Game {} - {} discard-then rider pushed for {}",
                    gameData.id, player.getUsername(), sourceCard.getName());
        }

        resumeRemainingEffectsAfterDiscard(gameData);
    }

    /**
     * Resumes the parked spell/ability once a discard flow has fully finished, then ends the
     * input through {@link InputCompletionService}.
     */
    private void resumeRemainingEffectsAfterDiscard(GameData gameData) {
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

        inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    /** Answers EXILE_FROM_HAND_CHOICE, including the multi-pick countdown carried on the record. */
    public void handleExileFromHandChosen(GameData gameData, Player player, int cardIndex) {
        PendingInteraction.ExileFromHandChoice exileChoice =
                gameData.interaction.activeInteraction(PendingInteraction.ExileFromHandChoice.class);
        if (exileChoice == null || !player.getId().equals(exileChoice.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        List<Integer> validIndices = exileChoice.validIndices();
        if (!validIndices.contains(cardIndex)) {
            log.warn("Game {} - {} sent invalid exile card index {}, re-prompting", gameData.id, player.getUsername(), cardIndex);
            playerInputService.beginExileFromHandChoice(gameData, player.getId(), exileChoice.sourcePermanentId(),
                    exileChoice.playPermissionControllerId(), exileChoice.remainingCount(),
                    exileChoice.remainingChoosers(), exileChoice.cardsPerPlayer(), exileChoice.faceDown(),
                    exileChoice.returnOnSourceLeave(), exileChoice.untapPermanentId());
            return;
        }

        UUID playerId = player.getId();
        UUID sourcePermanentId = exileChoice.sourcePermanentId();
        List<Card> hand = gameData.playerHands.get(playerId);
        Card card = hand.remove(cardIndex);

        // Add to player's exile zone, tracked with source permanent if available (e.g. Karn Liberated)
        if (sourcePermanentId != null && exileChoice.faceDown()) {
            exileService.exileCardFaceDown(gameData, playerId, card, sourcePermanentId);
        } else if (sourcePermanentId != null) {
            exileService.exileCard(gameData, playerId, card, sourcePermanentId);
        } else {
            exileService.exileCard(gameData, playerId, card);
        }

        // Grant the controlling player permission to play this card for as long as it remains
        // exiled (e.g. Fiend of the Shadows). Does not expire at end of turn.
        if (exileChoice.playPermissionControllerId() != null) {
            gameData.exilePlayPermissions.put(card.getId(), exileChoice.playPermissionControllerId());
        }

        if (exileChoice.returnOnSourceLeave() && sourcePermanentId != null) {
            gameData.addExileReturnOnPermanentLeave(sourcePermanentId,
                    new PendingExileReturn(card, playerId, false, true));
        }

        // A face-down exile must not name the card in the shared log (Gustha's Scepter).
        if (exileChoice.faceDown()) {
            gameLogService.append(gameData, GameLog.text(
                    player.getUsername() + " exiles a card from hand face down."));
        } else {
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " exiles ", card, " from hand."));
        }
        log.info("Game {} - {} exiles {} from hand", gameData.id, player.getUsername(), card.getName());

        int remainingExiles = Math.max(exileChoice.remainingCount() - 1, 0);

        if (remainingExiles > 0 && !hand.isEmpty()) {
            inputCompletionService.publishStateAfterInput(gameData);
            playerInputService.beginExileFromHandChoice(gameData, playerId, sourcePermanentId,
                    exileChoice.playPermissionControllerId(), remainingExiles,
                    exileChoice.remainingChoosers(), exileChoice.cardsPerPlayer(), exileChoice.faceDown(),
                    exileChoice.returnOnSourceLeave(), exileChoice.untapPermanentId());
        } else if (exileChoice.remainingChoosers() != null && !exileChoice.remainingChoosers().isEmpty()) {
            // Next opponent in the each-opponent exile queue (Nicol Bolas, God-Pharaoh +1).
            UUID next = exileChoice.remainingChoosers().getFirst();
            List<UUID> rest = exileChoice.remainingChoosers().size() > 1
                    ? List.copyOf(exileChoice.remainingChoosers().subList(1, exileChoice.remainingChoosers().size()))
                    : List.of();
            inputCompletionService.publishStateAfterInput(gameData);
            playerInputService.beginExileFromHandChoice(gameData, next, sourcePermanentId,
                    exileChoice.playPermissionControllerId(), exileChoice.cardsPerPlayer(), rest,
                    exileChoice.cardsPerPlayer(), exileChoice.faceDown(), exileChoice.returnOnSourceLeave(),
                    exileChoice.untapPermanentId());
        } else {
            gameData.interaction.clearAwaitingInput();

            if (exileChoice.untapPermanentId() != null) {
                Permanent source = gameQueryService.findPermanentById(gameData, exileChoice.untapPermanentId());
                if (source != null && tapUntapSupport.untapPermanent(gameData, source)) {
                    gameLogService.append(gameData, GameLog.cardThen(source.getCard(), " untaps."));
                }
            }

            // Resume resolving remaining effects
            if (gameData.pendingEffectResolutionEntry != null) {
                effectResolutionService.resolveEffectsFrom(gameData,
                        gameData.pendingEffectResolutionEntry,
                        gameData.pendingEffectResolutionIndex);
            }

            inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
        }
    }

    public void handleRevealedHandCardChosen(GameData gameData, Player player, int cardIndex) {
        PendingInteraction.RevealedHandChoice revealedHandChoice =
                gameData.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        if (revealedHandChoice == null || !player.getId().equals(revealedHandChoice.choosingPlayerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        // Optional choice declined (e.g. Vendilion Clique: "you may choose a nonland card").
        if (cardIndex == -1 && revealedHandChoice.optional()) {
            handleRevealedHandChoiceDeclined(gameData, player, revealedHandChoice);
            return;
        }

        List<Integer> validIndices = revealedHandChoice.validIndices();
        if (!validIndices.contains(cardIndex)) {
            throw new IllegalStateException("Invalid card index: " + cardIndex);
        }

        UUID targetPlayerId = revealedHandChoice.targetPlayerId();
        List<Card> targetHand = gameData.playerHands.get(targetPlayerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);

        Card chosenCard = targetHand.remove(cardIndex);
        List<Card> chosenCards = new ArrayList<>(revealedHandChoice.chosenCards());
        chosenCards.add(chosenCard);

        gameLogService.append(gameData, GameLog.textCardText(
                player.getUsername() + " chooses ", chosenCard, " from " + targetName + "'s hand."));
        log.info("Game {} - {} chooses {} from {}'s hand", gameData.id, player.getUsername(), chosenCard.getName(), targetName);

        int remainingChoices = Math.max(revealedHandChoice.remainingCount() - 1, 0);
        boolean discardMode = revealedHandChoice.discardMode();
        boolean exileMode = revealedHandChoice.exileMode();
        boolean bottomThenDrawMode = revealedHandChoice.bottomThenDrawMode();
        // Distended Mindbender: after the first filtered pick, begin a second pick under followUpFilter.
        if (remainingChoices == 0 && revealedHandChoice.followUpFilter() != null && !targetHand.isEmpty()) {
            CardPredicate followUp = revealedHandChoice.followUpFilter();
            List<Integer> followUpIndices = new ArrayList<>();
            for (int i = 0; i < targetHand.size(); i++) {
                if (predicateEvaluationService.matchesCardPredicate(targetHand.get(i), followUp, null)) {
                    followUpIndices.add(i);
                }
            }
            if (!followUpIndices.isEmpty()) {
                String followUpPrompt = revealedHandChoice.followUpPrompt() != null
                        ? revealedHandChoice.followUpPrompt()
                        : "Choose another card to discard.";
                interactionHandlerRegistry.begin(gameData, new PendingInteraction.RevealedHandChoice(
                        player.getId(), targetPlayerId, followUpIndices, 1,
                        discardMode, exileMode, chosenCards, revealedHandChoice.sourcePermanentId(),
                        followUpPrompt, false, false,
                        revealedHandChoice.gainLifeToChooserEqualToChosenToughness(), null, null));
                return;
            }
            // No second-band match — fall through and discard only what was already chosen.
        }

        // A choosableFilter (Reap Intellect) keeps its restriction on every follow-up pick.
        CardPredicate choosableFilter = revealedHandChoice.choosableFilter();
        List<Integer> newValidIndices = new ArrayList<>();
        for (int i = 0; i < targetHand.size(); i++) {
            if (choosableFilter == null
                    || predicateEvaluationService.matchesCardPredicate(targetHand.get(i), choosableFilter, null)) {
                newValidIndices.add(i);
            }
        }

        if (remainingChoices > 0 && !newValidIndices.isEmpty()) {
            String prompt;
            if (discardMode) {
                prompt = "Choose another card to discard.";
            } else if (exileMode) {
                prompt = "Choose another card to exile.";
            } else if (revealedHandChoice.shuffleIntoLibraryMode()) {
                prompt = "Choose another card to shuffle into " + targetName + "'s library.";
            } else {
                prompt = "Choose another card to put on top of " + targetName + "'s library.";
            }
            // Matching the legacy mid-flow re-begin, sourcePermanentId is not carried across picks.
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.RevealedHandChoice(
                    player.getId(), targetPlayerId, newValidIndices, remainingChoices,
                    discardMode, exileMode, chosenCards, null, prompt, false, revealedHandChoice.optional(),
                    false, null, null, 0, choosableFilter, revealedHandChoice.exileAllCopiesOfChosenNames(),
                    false, revealedHandChoice.shuffleIntoLibraryMode(), false,
                    revealedHandChoice.grantPlayPermission(), revealedHandChoice.returnAtNextEndStep(),
                    revealedHandChoice.exilePlayOpponentTax()));
        } else {
            finishRevealedHandChoice(gameData, player, revealedHandChoice, chosenCards);
        }
    }

    /**
     * Applies the batch action of a completed {@link PendingInteraction.RevealedHandChoice} (discard
     * / exile / bottom-then-draw / put on top of library / shuffle into library) and resumes the
     * interrupted resolution.
     */
    private void finishRevealedHandChoice(GameData gameData, Player player,
                                          PendingInteraction.RevealedHandChoice revealedHandChoice,
                                          List<Card> chosenCards) {
        UUID targetPlayerId = revealedHandChoice.targetPlayerId();
        String targetName = gameData.playerIdToName.get(targetPlayerId);
        boolean discardMode = revealedHandChoice.discardMode();
        boolean exileMode = revealedHandChoice.exileMode();
        boolean bottomThenDrawMode = revealedHandChoice.bottomThenDrawMode();
        boolean shuffleIntoLibraryMode = revealedHandChoice.shuffleIntoLibraryMode();
        boolean discardThenDrawMode = revealedHandChoice.discardThenDrawMode();

        gameData.interaction.clearAwaitingInput();

        if (discardMode) {
            // Talara's Bane: the chooser gains life equal to the chosen card's toughness before discard.
            if (revealedHandChoice.gainLifeToChooserEqualToChosenToughness()) {
                int toughness = chosenCards.stream()
                        .mapToInt(c -> c.getToughness() != null ? c.getToughness() : 0)
                        .sum();
                lifeSupport.applyGainLife(gameData, player.getId(), toughness);
            }

            // Discard chosen cards to graveyard (or battlefield if replacement effect applies)
            List<Card> replacedCards = new ArrayList<>();
            for (Card discarded : chosenCards) {
                if (hasEnterBattlefieldOnDiscardEffect(discarded) && gameData.discardCausedByOpponent) {
                    Permanent permanent = new Permanent(discarded);
                    battlefieldEntryService.putPermanentOntoBattlefield(gameData, targetPlayerId, permanent);
                    replacedCards.add(discarded);
                    gameLogService.append(gameData, GameLog.textCardText(
                            targetName + " discards ", discarded, " — it enters the battlefield instead."));
                    log.info("Game {} - {} discards {} — replacement effect puts it onto the battlefield",
                            gameData.id, targetName, discarded.getName());
                } else {
                    graveyardService.discardCard(gameData, targetPlayerId, discarded);
                }
            }

            List<Card> normallyDiscarded = chosenCards.stream()
                    .filter(c -> !replacedCards.contains(c))
                    .toList();
            if (!normallyDiscarded.isEmpty()) {
                String cardNames = String.join(", ", normallyDiscarded.stream().map(Card::getName).toList());
                gameLogService.append(gameData,
                        appendCards(GameLog.builder().text(targetName + " discards "), normallyDiscarded)
                                .text(".").build());
                log.info("Game {} - {} discards {} from {}'s hand", gameData.id, player.getUsername(), cardNames, targetName);
            }

            triggerCollectionService.beginDiscardEvent(gameData, targetPlayerId);
            for (Card discarded : chosenCards) {
                triggerCollectionService.checkDiscardTriggers(gameData, targetPlayerId, discarded);
            }
            triggerCollectionService.finishDiscardEvent(gameData);

            for (Card replaced : replacedCards) {
                if (replaced.hasType(CardType.CREATURE)) {
                    battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, targetPlayerId, replaced, null, false);
                }
            }

            if (discardThenDrawMode) {
                drawService.resolveDrawCard(gameData, targetPlayerId);
            }
        } else if (exileMode) {
            // Exile chosen cards
            UUID sourcePermanentId = revealedHandChoice.sourcePermanentId();
            PendingValkiHandExileChoice valkiChoice = gameData.pollPendingInteraction(
                    PendingValkiHandExileChoice.class);
            if (valkiChoice != null) {
                sourcePermanentId = valkiChoice.sourcePermanentId();
            }
            for (Card exiled : chosenCards) {
                if (valkiChoice != null) {
                    exileService.exileCard(gameData, targetPlayerId, exiled, sourcePermanentId);
                } else {
                    exileService.exileCard(gameData, targetPlayerId, exiled);
                }
                if (revealedHandChoice.imprintOnSource() && sourcePermanentId != null) {
                    exileService.setImprintedCardOnPermanent(gameData, sourcePermanentId, exiled);
                }
                if (revealedHandChoice.grantPlayPermission()) {
                    if (revealedHandChoice.exilePlayOpponentTax() > 0) {
                        exileSupport.grantPlayWhileExiledWithOpponentTax(
                                gameData, exiled.getId(), targetPlayerId, player.getId(),
                                revealedHandChoice.exilePlayOpponentTax());
                    } else {
                        gameData.exilePlayPermissions.put(exiled.getId(), player.getId());
                    }
                }
                if (revealedHandChoice.returnAtNextEndStep()) {
                    gameData.queueDelayedAction(new ReturnExiledCardToHandAtNextEndStep(
                            exiled.getId(), targetPlayerId));
                }
            }

            String cardNames = String.join(", ", chosenCards.stream().map(Card::getName).toList());
            gameLogService.append(gameData,
                    appendCards(GameLog.builder().text(player.getUsername() + " exiles "), chosenCards)
                            .text(" from " + targetName + "'s hand.").build());
            log.info("Game {} - {} exiles {} from {}'s hand", gameData.id, player.getUsername(), cardNames, targetName);

            // Track return-on-source-leave for exile-until-leaves effects (e.g. Kitesail Freebooter)
            if (sourcePermanentId != null && !revealedHandChoice.imprintOnSource()) {
                for (Card exiled : chosenCards) {
                    gameData.addExileReturnOnPermanentLeave(sourcePermanentId,
                            new PendingExileReturn(exiled, targetPlayerId, false, true));
                }
            }

            if (revealedHandChoice.exileAllCopiesOfChosenNames()) {
                exileSameNamedCopies(gameData, player, targetPlayerId, chosenCards);
            }
        } else if (shuffleIntoLibraryMode) {
            List<Card> deck = gameData.playerDecks.get(targetPlayerId);
            deck.addAll(chosenCards);
            LibraryShuffleHelper.shuffleLibrary(gameData, targetPlayerId);

            String cardNames = String.join(", ", chosenCards.stream().map(Card::getName).toList());
            gameLogService.append(gameData,
                    appendCards(GameLog.builder().text(targetName + " shuffles "), chosenCards)
                            .text(" into their library.").build());
            log.info("Game {} - {} shuffles {} into their library", gameData.id, targetName, cardNames);
        } else if (bottomThenDrawMode) {
            // Vendilion Clique: reveal chosen card, put it on the bottom of the library, then draw a card.
            List<Card> deck = gameData.playerDecks.get(targetPlayerId);
            for (Card chosen : chosenCards) {
                deck.addLast(chosen);
            }

            String cardNames = String.join(", ", chosenCards.stream().map(Card::getName).toList());
            gameLogService.append(gameData,
                    appendCards(GameLog.builder().text(targetName + " reveals "), chosenCards)
                            .text(", puts it on the bottom of their library, then draws a card.").build());
            log.info("Game {} - {} bottoms {} from {}'s hand and {} draws", gameData.id,
                    player.getUsername(), cardNames, targetName, targetName);

            drawService.resolveDrawCard(gameData, targetPlayerId);
        } else {
            // Put chosen cards on top of library
            List<Card> deck = gameData.playerDecks.get(targetPlayerId);

            // Insert in reverse order so first chosen ends up on top
            for (int i = chosenCards.size() - 1; i >= 0; i--) {
                deck.addFirst(chosenCards.get(i));
            }

            String cardNames = String.join(", ", chosenCards.stream().map(Card::getName).toList());
            gameLogService.append(gameData,
                    appendCards(GameLog.builder().text(player.getUsername() + " puts "), chosenCards)
                            .text(" on top of " + targetName + "'s library.").build());
            log.info("Game {} - {} puts {} on top of {}'s library", gameData.id, player.getUsername(), cardNames, targetName);
        }

        // Process any pending self-discard triggers (e.g. Guerrilla Tactics)
        if (gameData.hasPendingInteraction(PermanentChoiceContext.DiscardTriggerAnyTarget.class)) {
            triggerCollectionService.processNextDiscardSelfTrigger(gameData);
            return;
        }

        // Resume resolving remaining effects on the same spell/ability
        // (e.g. Thoughtseize: choose + discard a nonland card, then "you lose 2 life")
        if (gameData.pendingEffectResolutionEntry != null) {
            effectResolutionService.resolveEffectsFrom(gameData,
                    gameData.pendingEffectResolutionEntry,
                    gameData.pendingEffectResolutionIndex);
        }

        if (gameData.interaction.isAwaitingInput()) {
            return;
        }

        inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    /**
     * Exiles every card sharing a name with one of {@code chosenCards} from the target's hand,
     * graveyard, and library, then shuffles that library (Reap Intellect's follow-up search).
     */
    private void exileSameNamedCopies(GameData gameData, Player player, UUID targetPlayerId, List<Card> chosenCards) {
        String targetName = gameData.playerIdToName.get(targetPlayerId);
        List<String> names = chosenCards.stream().map(Card::getName).distinct().toList();

        int exiledCount = exileNamedCardsFromZone(gameData, targetPlayerId, gameData.playerHands.get(targetPlayerId), names);

        List<Card> graveyard = gameData.playerGraveyards.get(targetPlayerId);
        List<Card> exiledFromGraveyard = exileNamedCardsFromZoneCards(gameData, targetPlayerId, graveyard, names);
        if (!exiledFromGraveyard.isEmpty()) {
            graveyardService.notifyCardsExiledFromGraveyard(gameData, targetPlayerId, exiledFromGraveyard);
        }
        exiledCount += exiledFromGraveyard.size();

        List<Card> library = gameData.playerDecks.get(targetPlayerId);
        exiledCount += exileNamedCardsFromZone(gameData, targetPlayerId, library, names);
        if (library != null) {
            java.util.Collections.shuffle(library);
        }

        gameLogService.append(gameData, GameLog.text(player.getUsername() + " exiles " + exiledCount
                + " card" + (exiledCount != 1 ? "s" : "") + " with the same name from " + targetName
                + "'s hand, graveyard, and library. " + targetName + " shuffles their library."));
        log.info("Game {} - {} exiled {} same-named card(s) from {}'s zones", gameData.id,
                player.getUsername(), exiledCount, targetName);
    }

    private int exileNamedCardsFromZone(GameData gameData, UUID ownerId, List<Card> zone, List<String> names) {
        return exileNamedCardsFromZoneCards(gameData, ownerId, zone, names).size();
    }

    private List<Card> exileNamedCardsFromZoneCards(GameData gameData, UUID ownerId, List<Card> zone,
                                                     List<String> names) {
        if (zone == null) {
            return List.of();
        }
        List<Card> toExile = zone.stream().filter(c -> names.contains(c.getName())).toList();
        zone.removeAll(toExile);
        toExile.forEach(card -> exileService.exileCard(gameData, ownerId, card));
        return toExile;
    }

    /** The caster declines an optional revealed-hand choice (e.g. Vendilion Clique's "may"). */
    private void handleRevealedHandChoiceDeclined(GameData gameData, Player player,
                                                  PendingInteraction.RevealedHandChoice revealedHandChoice) {
        // "Up to X" flows that already picked something still apply the batch action for those picks.
        if (!revealedHandChoice.chosenCards().isEmpty()) {
            finishRevealedHandChoice(gameData, player, revealedHandChoice, revealedHandChoice.chosenCards());
            return;
        }

        gameData.interaction.clearAwaitingInput();

        String targetName = gameData.playerIdToName.get(revealedHandChoice.targetPlayerId());
        String declineLog = player.getUsername() + " chooses no card from " + targetName + "'s hand.";
        gameLogService.append(gameData, GameLog.text(declineLog));
        log.info("Game {} - {} declines the revealed-hand choice", gameData.id, player.getUsername());

        // Nightsnare: declining makes the target discard cards of their own choice instead.
        if (revealedHandChoice.declineFallbackDiscardCount() > 0) {
            gameData.discardCausedByOpponent = true;
            playerInteractionSupport.resolveDiscardCards(gameData, revealedHandChoice.targetPlayerId(),
                    revealedHandChoice.declineFallbackDiscardCount());
            if (gameData.interaction.isAwaitingInput()) {
                return;
            }
        }

        // Resume resolving remaining effects on the same spell/ability.
        if (gameData.pendingEffectResolutionEntry != null) {
            effectResolutionService.resolveEffectsFrom(gameData,
                    gameData.pendingEffectResolutionEntry,
                    gameData.pendingEffectResolutionIndex);
        }

        if (gameData.interaction.isAwaitingInput()) {
            return;
        }

        inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    /**
     * Answers the Blackmail / Noggin Whack / Thieving Sprite flow
     * ({@link PendingInteraction.RevealCardsDiscardChoice}). In the
     * reveal stage the target player picks a card to reveal (transitioning to the controller's
     * discard choice once the last one is revealed); in the discard stage the controller picks one
     * revealed card and the target discards it.
     */
    public void handleRevealCardsDiscardChosen(GameData gameData, Player player, int cardIndex) {
        PendingInteraction.RevealCardsDiscardChoice choice =
                gameData.interaction.activeInteraction(PendingInteraction.RevealCardsDiscardChoice.class);
        if (choice == null || !player.getId().equals(choice.decidingPlayerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }
        if (!choice.validIndices().contains(cardIndex)) {
            throw new IllegalStateException("Invalid card index: " + cardIndex);
        }

        UUID targetPlayerId = choice.targetPlayerId();
        List<Card> targetHand = gameData.playerHands.get(targetPlayerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);

        if (choice.revealStage()) {
            // The target player picks a card (by hand index) to reveal.
            Card chosen = targetHand.get(cardIndex);
            List<UUID> revealed = new ArrayList<>(choice.revealedCardIds());
            revealed.add(chosen.getId());
            int remaining = Math.max(choice.remainingCount() - 1, 0);

            if (remaining > 0) {
                List<Integer> newValid = new ArrayList<>(choice.validIndices());
                newValid.remove(Integer.valueOf(cardIndex));
                interactionHandlerRegistry.begin(gameData, new PendingInteraction.RevealCardsDiscardChoice(
                        choice.decidingPlayerId(), targetPlayerId, choice.controllerId(), true,
                        newValid, remaining, revealed, choice.discardCount(), choice.destination(),
                        choice.sourcePermanentId()));
            } else {
                gameData.interaction.clearAwaitingInput();
                playerInteractionSupport.beginRevealCardsDiscardStage(gameData, targetPlayerId,
                        choice.controllerId(), revealed, choice.discardCount(), choice.destination(),
                        choice.sourcePermanentId());
            }
            return;
        }

        // Discard stage: cardIndex is into the revealed set; map it back to the hand.
        UUID chosenId = choice.revealedCardIds().get(cardIndex);
        int handIndex = -1;
        for (int i = 0; i < targetHand.size(); i++) {
            if (targetHand.get(i).getId().equals(chosenId)) {
                handIndex = i;
                break;
            }
        }
        gameData.interaction.clearAwaitingInput();
        if (handIndex >= 0) {
            Card card = targetHand.remove(handIndex);
            String controllerName = player.getUsername();

            if (choice.destination() == HandChoiceDestination.EXILE) {
                if (choice.sourcePermanentId() != null) {
                    exileService.exileCard(gameData, targetPlayerId, card, choice.sourcePermanentId());
                } else {
                    exileService.exileCard(gameData, targetPlayerId, card);
                }
                gameLogService.append(gameData, GameLog.textCardText(
                        controllerName + " chooses ", card, " and exiles it from " + targetName + "'s hand."));
                log.info("Game {} - {} exiles {} from {}'s hand", gameData.id, controllerName, card.getName(), targetName);
            } else if (hasEnterBattlefieldOnDiscardEffect(card) && gameData.discardCausedByOpponent) {
                Permanent permanent = new Permanent(card);
                battlefieldEntryService.putPermanentOntoBattlefield(gameData, targetPlayerId, permanent);
                gameLogService.append(gameData, GameLog.textCardText(
                        targetName + " discards ", card, " — it enters the battlefield instead."));
                log.info("Game {} - {} discards {} — replacement effect puts it onto the battlefield",
                        gameData.id, targetName, card.getName());
                triggerCollectionService.checkDiscardTriggers(gameData, targetPlayerId, card);
                if (card.hasType(CardType.CREATURE)) {
                    battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, targetPlayerId, card, null, false);
                }
            } else {
                graveyardService.discardCard(gameData, targetPlayerId, card);
                gameLogService.append(gameData, GameLog.textCardText(
                        controllerName + " chooses ", card, "; " + targetName + " discards it."));
                log.info("Game {} - {} discards {} (chosen by {})", gameData.id, targetName, card.getName(), controllerName);
                triggerCollectionService.checkDiscardTriggers(gameData, targetPlayerId, card);
            }
        }

        // More cards left to discard (e.g. Noggin Whack chooses two)? Prompt for the next one over
        // the remaining revealed cards before resolving any triggers or the rest of the spell.
        int remainingDiscards = choice.remainingCount() - 1;
        List<UUID> remainingRevealed = new ArrayList<>(choice.revealedCardIds());
        remainingRevealed.remove(chosenId);
        if (remainingDiscards > 0 && !remainingRevealed.isEmpty()) {
            playerInteractionSupport.beginRevealCardsDiscardStageContinuation(gameData, targetPlayerId,
                    choice.controllerId(), remainingRevealed, remainingDiscards, choice.discardCount(),
                    choice.destination(), choice.sourcePermanentId());
            return;
        }

        // Process any pending self-discard triggers (e.g. Guerrilla Tactics)
        if (gameData.hasPendingInteraction(PermanentChoiceContext.DiscardTriggerAnyTarget.class)) {
            triggerCollectionService.processNextDiscardSelfTrigger(gameData);
            return;
        }

        // Resume any remaining effects on the same spell/ability.
        if (gameData.pendingEffectResolutionEntry != null) {
            effectResolutionService.resolveEffectsFrom(gameData,
                    gameData.pendingEffectResolutionEntry,
                    gameData.pendingEffectResolutionIndex);
        }

        inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    /**
     * Answers Struggle for Sanity's alternating exile
     * ({@link PendingInteraction.AlternatingHandExileChoice}). The picked card is exiled from the
     * target's hand into the deciding player's pile; the next pick goes to the other player. Once
     * the hand is empty the target's own pile returns to their hand and the controller's pile goes
     * to the target's graveyard.
     */
    public void handleAlternatingHandExileChosen(GameData gameData, Player player, int cardIndex) {
        PendingInteraction.AlternatingHandExileChoice choice =
                gameData.interaction.activeInteraction(PendingInteraction.AlternatingHandExileChoice.class);
        if (choice == null || !player.getId().equals(choice.decidingPlayerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }
        if (!choice.validIndices().contains(cardIndex)) {
            throw new IllegalStateException("Invalid card index: " + cardIndex);
        }

        UUID targetPlayerId = choice.targetPlayerId();
        UUID controllerId = choice.controllerId();
        List<Card> targetHand = gameData.playerHands.get(targetPlayerId);
        if (targetHand == null || cardIndex >= targetHand.size()) {
            throw new IllegalStateException("Invalid card index: " + cardIndex);
        }

        boolean targetPicked = choice.decidingPlayerId().equals(targetPlayerId);
        Card exiled = targetHand.remove(cardIndex);
        exileService.exileCard(gameData, targetPlayerId, exiled);

        List<UUID> targetExiled = new ArrayList<>(choice.targetExiledIds());
        List<UUID> controllerExiled = new ArrayList<>(choice.controllerExiledIds());
        (targetPicked ? targetExiled : controllerExiled).add(exiled.getId());
        gameLogService.append(gameData, GameLog.textCardText(
                player.getUsername() + " exiles ", exiled, "."));

        gameData.interaction.clearAwaitingInput();
        UUID nextChooser = targetPicked ? controllerId : targetPlayerId;
        if (playerInteractionSupport.beginAlternatingHandExile(gameData, nextChooser, targetPlayerId,
                controllerId, targetExiled, controllerExiled)) {
            return;
        }

        for (UUID cardId : targetExiled) {
            ExiledCardEntry entry = gameData.findExiledCard(cardId);
            if (entry != null) {
                gameData.removeFromExile(cardId);
                gameData.addCardToHand(targetPlayerId, entry.card());
            }
        }
        for (UUID cardId : controllerExiled) {
            ExiledCardEntry entry = gameData.findExiledCard(cardId);
            if (entry != null) {
                gameData.removeFromExile(cardId);
                graveyardService.addCardToGraveyard(gameData, targetPlayerId, entry.card());
            }
        }
        log.info("Game {} - alternating hand exile finished: {} cards returned, {} to graveyard",
                gameData.id, targetExiled.size(), controllerExiled.size());

        if (gameData.pendingEffectResolutionEntry != null) {
            effectResolutionService.resolveEffectsFrom(gameData,
                    gameData.pendingEffectResolutionEntry,
                    gameData.pendingEffectResolutionIndex);
        }
        if (gameData.interaction.isAwaitingInput()) {
            return;
        }
        inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    /** Appends {@code cards} as comma-separated card segments (each hoverable) to {@code builder}. */
    private static GameLog.Builder appendCards(GameLog.Builder builder, List<Card> cards) {
        for (int i = 0; i < cards.size(); i++) {
            if (i > 0) {
                builder.text(", ");
            }
            builder.card(cards.get(i));
        }
        return builder;
    }

    /** Answers IMPRINT_FROM_HAND_CHOICE (exile the chosen card and imprint it on the source permanent). */
    public void handleImprintFromHandCardChosen(GameData gameData, Player player, int cardIndex) {
        PendingInteraction.ImprintFromHandChoice imprintChoice =
                gameData.interaction.activeInteraction(PendingInteraction.ImprintFromHandChoice.class);
        if (imprintChoice == null || !player.getId().equals(imprintChoice.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        List<Integer> validIndices = imprintChoice.validIndices();
        if (!validIndices.contains(cardIndex)) {
            log.warn("Game {} - {} sent invalid imprint card index {}, re-prompting", gameData.id, player.getUsername(), cardIndex);
            playerInputService.beginImprintFromHandChoice(gameData, player.getId(),
                    new ArrayList<>(validIndices), "Choose a card from your hand.", imprintChoice.sourcePermanentId(),
                    imprintChoice.grantCastPermission(), imprintChoice.faceDown());
            return;
        }

        UUID playerId = player.getId();
        UUID sourcePermanentId = imprintChoice.sourcePermanentId();

        gameData.interaction.clearAwaitingInput();

        List<Card> hand = gameData.playerHands.get(playerId);
        Card card = hand.remove(cardIndex);

        if (imprintChoice.faceDown()) {
            exileService.exileCardFaceDown(gameData, playerId, card, null);
        } else {
            exileService.exileCard(gameData, playerId, card);
        }

        // "You may cast that card for as long as it remains exiled" (Ice Cauldron) — no expiry.
        if (imprintChoice.grantCastPermission()) {
            gameData.exilePlayPermissions.put(card.getId(), playerId);
        }

        // Imprint on source permanent
        Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (sourcePermanent != null) {
            gameData.setImprintedCard(sourcePermanent.getCard(), card);

            if (imprintChoice.faceDown()) {
                gameLogService.append(gameData, GameLog.textCardText(
                        player.getUsername() + " exiles a card face down and imprints it on ",
                        sourcePermanent.getCard(), "."));
            } else {
                gameLogService.append(gameData, GameLog.cardTextCard(
                        card, " is exiled and imprinted on ", sourcePermanent.getCard(), "."));
            }
            log.info("Game {} - {} imprinted {} from hand on {}", gameData.id, player.getUsername(), card.getName(), sourcePermanent.getCard().getName());
        } else {
            if (imprintChoice.faceDown()) {
                gameLogService.append(gameData, GameLog.text(
                        player.getUsername() + " exiles a card face down (source permanent no longer on the battlefield)."));
            } else {
                gameLogService.append(gameData,
                        GameLog.cardThen(card, " is exiled (source permanent no longer on the battlefield)."));
            }
            log.info("Game {} - Source permanent left battlefield, {} exiled without imprinting", gameData.id, card.getName());
        }

        inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    public void handleExileFromHandWithRefineCountersChosen(GameData gameData, Player player, int cardIndex) {
        PendingInteraction.ExileFromHandWithRefineCountersChoice choice =
                gameData.interaction.activeInteraction(PendingInteraction.ExileFromHandWithRefineCountersChoice.class);
        if (choice == null || !player.getId().equals(choice.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        if (!choice.validIndices().contains(cardIndex)) {
            log.warn("Game {} - {} sent invalid refine-counter card index {}, re-prompting",
                    gameData.id, player.getUsername(), cardIndex);
            playerInputService.beginExileFromHandWithRefineCountersChoice(gameData, player.getId(),
                    new ArrayList<>(choice.validIndices()), "Choose a card from your hand to exile.",
                    choice.counterCount());
            return;
        }

        gameData.interaction.clearAwaitingInput();
        List<Card> hand = gameData.playerHands.get(player.getId());
        Card card = hand.remove(cardIndex);
        exileService.exileCard(gameData, player.getId(), card);
        gameData.exiledCardRefineCounters.put(card.getId(), choice.counterCount());
        gameLogService.append(gameData, GameLog.cardThen(card,
                " is exiled with " + choice.counterCount() + " refine counters."));
        inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void resolveTargetedCardChoice(GameData gameData, Player player, UUID playerId, Card card, UUID targetId) {
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target != null) {
            Permanent auraPerm = new Permanent(card);
            auraPerm.setAttachedTo(target.getId());
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, playerId, auraPerm);

            gameLogService.append(gameData, GameLog.builder()
                    .text(player.getUsername() + " puts ")
                    .card(card)
                    .text(" onto the battlefield attached to ")
                    .card(target.getCard())
                    .text(".")
                    .build());
            log.info("Game {} - {} puts {} onto the battlefield attached to {}", gameData.id, player.getUsername(), card.getName(), target.getCard().getName());
        } else {
            gameData.addCardToHand(playerId, card);
            gameLogService.append(gameData,
                    GameLog.cardThen(card, " can't be attached (target left the battlefield)."));
            log.info("Game {} - Aura target gone, {} returned to hand", gameData.id, card.getName());
        }
    }

    private Permanent resolveUntargetedCardChoice(GameData gameData, Player player, UUID playerId, Card card,
                                             boolean enterTapped, boolean grantHaste, boolean sacrificeAtEndStep,
                                             boolean returnToHandAtEndStep,
                                             UUID attachEquipmentCardId, boolean enterAttacking,
                                             Integer sacrificeUnlessPayGenericReduction, boolean faceDown,
                                             int faceDownPower, int faceDownToughness,
                                             Set<CardType> faceDownCardTypes,
                                             UUID returnExiledSourceCardId) {
        Permanent permanent = new Permanent(card);
        if (faceDown) {
            permanent.setFaceDown(faceDownPower, faceDownToughness, faceDownCardTypes);
        }
        if (enterTapped) {
            permanent.tap();
        }
        if (grantHaste) {
            permanent.getGrantedKeywords().add(Keyword.HASTE);
        }
        UUID attackTargetId = enterAttacking && gameData.pendingEffectResolutionEntry != null
                ? gameData.pendingEffectResolutionEntry.getAttackedTargetId() : null;
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, playerId, permanent);
        if (enterAttacking) {
            permanent.setAttacking(true);
            permanent.setAttackTarget(attackTargetId);
        }

        String stateSuffix = enterTapped && enterAttacking ? " tapped and attacking"
                : enterTapped ? " tapped"
                : enterAttacking ? " attacking"
                : "";
        if (faceDown) {
            gameLogService.append(gameData, GameLog.text(
                    player.getUsername() + " puts a card onto the battlefield face down" + stateSuffix + "."));
        } else {
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " puts ", card, " onto the battlefield" + stateSuffix + "."));
        }
        log.info("Game {} - {} puts {} onto the battlefield{}", gameData.id, player.getUsername(), card.getName(),
                stateSuffix);

        if (!faceDown) {
            battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, playerId, card, null, false);
        }

        // Deathrender: "…and attach this Equipment to it" — attach the source Equipment to the entered creature.
        if (attachEquipmentCardId != null) {
            attachSourceEquipmentToPermanent(gameData, attachEquipmentCardId, permanent);
        }

        if (sacrificeAtEndStep) {
            gameData.queueDelayedAction(new DelayedPermanentAction(permanent.getId(),
                    DelayedPermanentActionKind.SACRIFICE_AT_END_STEP, false, returnExiledSourceCardId));
        }
        if (returnToHandAtEndStep) {
            gameData.queueDelayedAction(new DelayedPermanentAction(permanent.getId(),
                    DelayedPermanentActionKind.RETURN_TO_HAND_AT_END_STEP));
        }

        // Flash: "sacrifice it unless you pay its mana cost reduced by {N}." Prompt a pay-or-sacrifice
        // may ability against the just-entered creature — accepting charges the reduced cost, declining
        // (or being unable to pay) sacrifices it via the ForcedCostOrElse SacrificeSelf penalty.
        if (sacrificeUnlessPayGenericReduction != null) {
            String reducedCost = reduceGenericCost(card.getManaCost(), sacrificeUnlessPayGenericReduction);
            ForcedCostOrElseEffect payOrSacrifice = new ForcedCostOrElseEffect(
                    new PayManaCost(reducedCost), List.of(new SacrificeSelfEffect()), true);
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    card, playerId, List.of(payOrSacrifice),
                    card.getName() + " - Pay " + reducedCost + " or sacrifice it?",
                    null, reducedCost, permanent.getId()));
            playerInputService.processNextMayAbility(gameData);
        }
        return permanent;
    }

    /**
     * Returns a mana cost string with its generic portion reduced by {@code reduction} (floored at 0);
     * colored, hybrid, and other symbols are preserved. Yields {@code "{0}"} when nothing else remains.
     */
    private static String reduceGenericCost(String manaCost, int reduction) {
        String cost = manaCost == null ? "" : manaCost;
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\{([^}]+)}").matcher(cost);
        int generic = 0;
        StringBuilder others = new StringBuilder();
        while (matcher.find()) {
            String symbol = matcher.group(1);
            try {
                generic += Integer.parseInt(symbol);
            } catch (NumberFormatException notGeneric) {
                others.append('{').append(symbol).append('}');
            }
        }
        int newGeneric = Math.max(0, generic - reduction);
        StringBuilder result = new StringBuilder();
        if (newGeneric > 0 || others.length() == 0) {
            result.append('{').append(newGeneric).append('}');
        }
        result.append(others);
        return result.toString();
    }

    private void attachSourceEquipmentToPermanent(GameData gameData, UUID equipmentCardId, Permanent target) {
        Permanent equipment = equipSupport.findEquipmentByCardId(gameData, equipmentCardId);
        if (equipment == null) {
            return;
        }
        if (!equipSupport.canAttachEquipment(gameData, equipment, target)) {
            return;
        }
        UUID oldAttachedTo = equipment.getAttachedTo();
        gameData.expireFloatingEffectsForUnattachedSource(equipment.getId());
        equipment.setAttachedTo(target.getId());
        // CR 613.7e: an Equipment receives a new timestamp each time it becomes attached.
        equipment.setTimestamp(gameData.nextTimestamp());
        equipSupport.applySacrificeOnUnattachIfNeeded(gameData, equipment, oldAttachedTo, target.getId());
        equipSupport.expireAttachedCopyEffects(gameData, equipment);
        equipment.setAttachedTo(target.getId());
        // CR 613.7e: an Equipment receives a new timestamp each time it becomes attached.
        equipment.setTimestamp(gameData.nextTimestamp());
        equipSupport.notifyEquipmentAttached(gameData, equipment, oldAttachedTo);

        gameLogService.append(gameData, GameLog.cardTextCard(
                equipment.getCard(), " is now attached to ", target.getCard(), "."));
        log.info("Game {} - {} attached to {}", gameData.id, equipment.getCard().getName(), target.getCard().getName());
    }

    private void checkPendingReturnToHandOnDiscard(GameData gameData, Card discardedCard) {
        PendingReturnToHandOnDiscardType pending = gameData.pendingReturnToHandOnDiscardType;
        if (pending == null || !discardedCard.hasType(pending.requiredType())) {
            return;
        }
        gameData.pendingReturnToHandOnDiscardType = null;
        // CR 608.2n: the spell leaves the stack only as the final part of its resolution, so record
        // the destination on the parked entry and let the single disposition site in
        // StackResolutionService move it. Moving it here as well would put it into the hand and then
        // let the deferred disposition drop a second copy into the graveyard.
        StackEntry parked = gameData.pendingEffectResolutionEntry;
        if (parked != null && parked.getCard() == pending.card()) {
            parked.setReturnToHandAfterResolving(true);
            return;
        }
        gameData.addCardToHand(pending.controllerId(), pending.card());
        gameLogService.append(gameData,
                GameLog.cardThen(pending.card(), " is returned to its owner's hand."));
        log.info("Game {} - {} returned to hand (matching card discarded)", gameData.id, pending.card().getName());
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
            if (!gameQueryService.cantBecomeUntapped(gameData, source)) {
                source.untap();
                gameLogService.append(gameData, GameLog.cardThen(source.getCard(), " untaps."));
                log.info("Game {} - {} untaps (creature discarded)", gameData.id, source.getCard().getName());
            }

            // Transform
            Card originalCard = source.getOriginalCard();
            Card backFace = originalCard.getBackFaceCard();
            if (backFace != null && !source.isTransformed()) {
                Card frontCard = source.getCard();
                source.setCard(backFace);
                source.setTransformed(true);
                gameLogService.append(gameData,
                        GameLog.cardTextCard(frontCard, " transforms into ", backFace, "."));
                log.info("Game {} - {} transforms into {}", gameData.id, frontCard.getName(), backFace.getName());
            }
        }
    }

    private void checkPendingUntapOnDiscardType(GameData gameData, Card discardedCard) {
        PendingUntapOnDiscardType pending = gameData.pendingUntapOnDiscardType;
        if (pending == null) {
            return;
        }
        gameData.pendingUntapOnDiscardType = null;
        if (!discardedCard.hasType(pending.requiredType())) {
            return;
        }
        Permanent source = gameQueryService.findPermanentById(gameData, pending.sourcePermanentId());
        if (source == null) {
            return;
        }
        source.untap();
        gameLogService.append(gameData, GameLog.cardThen(source.getCard(), " untaps."));
        log.info("Game {} - {} untaps (matching card type discarded)", gameData.id, source.getCard().getName());
    }

    private void checkPendingBoostSourceByDiscardedManaValue(GameData gameData, Card discardedCard) {
        PendingBoostSourceByDiscardedManaValue pending = gameData.pendingBoostSourceByDiscardedManaValue;
        if (pending == null) {
            return;
        }
        gameData.pendingBoostSourceByDiscardedManaValue = null;
        int boost = discardedCard.getManaValue();
        if (boost <= 0) {
            return;
        }
        Permanent source = gameQueryService.findPermanentById(gameData, pending.sourcePermanentId());
        if (source == null) {
            return;
        }
        source.setPowerModifier(source.getPowerModifier() + boost);
        gameLogService.append(gameData, GameLog.builder()
                .card(source.getCard())
                .text(String.format(" gets +%d/+0 until end of turn.", boost))
                .build());
        log.info("Game {} - {} gets +{}/+0 (discarded card mana value)", gameData.id, source.getCard().getName(), boost);
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
        gameData.pendingReturnToHandOnDiscardType = null;
        // No matching card type was discarded — the spell takes its default graveyard disposition as
        // the final part of its resolution (CR 608.2n). Only dispose of it here when no parked entry
        // is still going to; otherwise the graveyard would end up with two copies of the card.
        StackEntry parked = gameData.pendingEffectResolutionEntry;
        if (parked != null && parked.getCard() == pending.card()) {
            return;
        }
        graveyardService.addCardToGraveyard(gameData, pending.controllerId(), pending.card());
    }
}


