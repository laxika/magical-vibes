package com.github.laxika.magicalvibes.service.turn;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ManaProducingEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.StackResolutionService;
import com.github.laxika.magicalvibes.service.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.combat.CombatAttackService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Automatically passes priority for players who have no playable cards or
 * activatable abilities, advancing the turn through steps until a player
 * can (or must) act.
 *
 * <p>Extracted from {@code TurnProgressionService} to isolate the priority-
 * resolution loop.  Also handles auto-resolution of combat triggers when
 * neither player can respond.
 */
@Slf4j
@Service
public class AutoPassService {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final TriggerCollectionService triggerCollectionService;
    private final StackResolutionService stackResolutionService;
    private final StepTriggerService stepTriggerService;
    private final CombatAttackService combatAttackService;

    public AutoPassService(
            GameQueryService gameQueryService,
            GameBroadcastService gameBroadcastService,
            TriggerCollectionService triggerCollectionService,
            StackResolutionService stackResolutionService,
            StepTriggerService stepTriggerService,
            CombatAttackService combatAttackService) {
        this.gameQueryService = gameQueryService;
        this.gameBroadcastService = gameBroadcastService;
        this.triggerCollectionService = triggerCollectionService;
        this.stackResolutionService = stackResolutionService;
        this.stepTriggerService = stepTriggerService;
        this.combatAttackService = combatAttackService;
    }

    /**
     * Main auto-pass loop.  Processes any pending triggers (spell-target,
     * discard-self, attack, death), then iterates: if the stack is non-empty
     * the loop stops (players must explicitly pass); if the priority holder
     * has nothing to play and no auto-stop is configured for the current step,
     * the player is auto-passed.  When both players pass, the step advances.
     *
     * <p>Contains a safety limit of 100 iterations to prevent infinite loops.
     *
     * @param gameData    the current game state to modify
     * @param advanceStep callback to advance to the next turn step
     */
    public void resolveAutoPass(GameData gameData, Consumer<GameData> advanceStep) {
        if (gameData.status != GameStatus.RUNNING) return;

        // Process any pending spell-target triggers (e.g. Livewire Lash)
        if (!gameData.interaction.isAwaitingInput() && !gameData.pendingSpellTargetTriggers.isEmpty()) {
            triggerCollectionService.processNextSpellTargetTrigger(gameData);
        }

        // Process any pending spell-cast graveyard-target triggers (e.g. Teshar, Ancestor's Apostle)
        if (!gameData.interaction.isAwaitingInput() && !gameData.pendingSpellGraveyardTargetTriggers.isEmpty()) {
            triggerCollectionService.processNextSpellGraveyardTargetTrigger(gameData);
        }

        // Process any pending discard self-triggers before death triggers
        if (!gameData.interaction.isAwaitingInput() && !gameData.pendingDiscardSelfTriggers.isEmpty()) {
            triggerCollectionService.processNextDiscardSelfTrigger(gameData);
        }

        // Process any pending targeted attack triggers before death triggers
        if (!gameData.interaction.isAwaitingInput() && !gameData.pendingAttackTriggerTargets.isEmpty()) {
            triggerCollectionService.processNextAttackTriggerTarget(gameData);
        }

        // Process any pending targeted death triggers before auto-passing
        if (!gameData.interaction.isAwaitingInput() && !gameData.pendingDeathTriggerTargets.isEmpty()) {
            triggerCollectionService.processNextDeathTriggerTarget(gameData);
        }

        // Process any pending explore targeted triggers
        if (!gameData.interaction.isAwaitingInput() && !gameData.pendingExploreTriggerTargets.isEmpty()) {
            triggerCollectionService.processNextExploreTriggerTarget(gameData);
        }

        // Process any pending life-gain targeted triggers
        if (!gameData.interaction.isAwaitingInput() && !gameData.pendingLifeGainTriggerTargets.isEmpty()) {
            triggerCollectionService.processNextLifeGainTriggerTarget(gameData);
        }

        // Process any pending saga chapter targeted triggers
        if (!gameData.interaction.isAwaitingInput() && !gameData.pendingSagaChapterTargets.isEmpty()) {
            triggerCollectionService.processNextSagaChapterTarget(gameData);
        }

        // Process any pending end-step targeted triggers
        if (!gameData.interaction.isAwaitingInput() && !gameData.pendingEndStepTriggerTargets.isEmpty()) {
            stepTriggerService.processNextEndStepTriggerTarget(gameData);
        }

        for (int safety = 0; safety < 100; safety++) {
            if (gameData.interaction.isAwaitingInput()) {
                gameBroadcastService.broadcastGameState(gameData);
                return;
            }
            if (gameData.status == GameStatus.FINISHED) return;

            // When stack is non-empty, never auto-pass — players must explicitly pass
            if (!gameData.stack.isEmpty()) {
                gameBroadcastService.broadcastGameState(gameData);
                return;
            }

            UUID priorityHolder = gameQueryService.getPriorityPlayerId(gameData);

            // If no one holds priority (both already passed), advance the step
            if (priorityHolder == null) {
                advanceStep.accept(gameData);
                continue;
            }

            List<Integer> playable = gameBroadcastService.getPlayableCardIndices(gameData, priorityHolder);
            if (!playable.isEmpty()) {
                // Priority holder can act — stop and let them decide
                gameBroadcastService.broadcastGameState(gameData);
                return;
            }

            // After blockers are declared, stop for the attacking player so they can
            // respond to blocks (e.g. cast combat tricks or activate abilities).
            if (gameData.currentStep == TurnStep.DECLARE_BLOCKERS
                    && priorityHolder.equals(gameData.activePlayerId)
                    && hasBlockingCreatures(gameData)) {
                gameBroadcastService.broadcastGameState(gameData);
                return;
            }

            // Never auto-pass the active player through DECLARE_ATTACKERS when
            // an opponent's effect forces them to attack (e.g. Trove of Temptation)
            // and attackers have not yet been declared this combat.
            if (gameData.currentStep == TurnStep.DECLARE_ATTACKERS
                    && priorityHolder.equals(gameData.activePlayerId)
                    && !hasAttackingCreatures(gameData, priorityHolder)
                    && combatAttackService.isOpponentForcedToAttack(gameData, priorityHolder)
                    && !combatAttackService.getAttackableCreatureIndices(gameData, priorityHolder).isEmpty()) {
                gameBroadcastService.broadcastGameState(gameData);
                return;
            }

            // Check if current step is in the priority holder's auto-stop set
            java.util.Set<TurnStep> stopSteps = gameData.playerAutoStopSteps.get(priorityHolder);
            if (stopSteps != null && stopSteps.contains(gameData.currentStep)) {
                gameBroadcastService.broadcastGameState(gameData);
                return;
            }

            // Priority holder has nothing to play — auto-pass for them
            String playerName = gameData.playerIdToName.get(priorityHolder);
            log.info("Game {} - Auto-passing priority for {} on step {} (no playable cards)",
                    gameData.id, playerName, gameData.currentStep);

            gameData.priorityPassedBy.add(priorityHolder);

            if (gameData.priorityPassedBy.size() >= 2) {
                advanceStep.accept(gameData);
            } else {
                gameBroadcastService.broadcastGameState(gameData);
            }
        }

        // Safety: if we somehow looped 100 times, broadcast current state and stop
        log.warn("Game {} - resolveAutoPass hit safety limit", gameData.id);
        gameBroadcastService.broadcastGameState(gameData);
    }

    /**
     * Auto-passes priority specifically for combat-triggered abilities on the
     * stack.  If neither player can respond (no playable cards or instant-speed
     * abilities), resolves each trigger in sequence.  Stops when the stack
     * empties, a player can respond, or user input is required.
     *
     * @param gameData the current game state to modify
     */
    public void resolveAutoPassCombatTriggers(GameData gameData) {
        for (int safety = 0; safety < 100; safety++) {
            if (gameData.stack.isEmpty()) return;
            if (gameData.interaction.isAwaitingInput()) return;
            if (gameData.status == GameStatus.FINISHED) return;

            UUID stackPriorityHolder = gameQueryService.getPriorityPlayerId(gameData);
            if (stackPriorityHolder == null) {
                // Both passed — resolve top of stack
                stackResolutionService.resolveTopOfStack(gameData);
                // After resolution, if user interaction is needed (e.g. multi-permanent choice), stop
                if (gameData.interaction.isAwaitingInput() || !gameData.pendingMayAbilities.isEmpty()) {
                    return;
                }
                gameData.priorityPassedBy.clear();
                continue;
            }

            List<Integer> playable = gameBroadcastService.getPlayableCardIndices(gameData, stackPriorityHolder);
            boolean hasActivatable = hasInstantSpeedActivatedAbility(gameData, stackPriorityHolder);

            if (!playable.isEmpty() || hasActivatable) {
                // Player can respond to the triggered ability — stop and let them
                gameBroadcastService.broadcastGameState(gameData);
                return;
            }

            // Auto-pass for this player
            gameData.priorityPassedBy.add(stackPriorityHolder);
        }
    }

    /**
     * Checks whether the given player has any creatures currently declared as attackers.
     */
    private boolean hasAttackingCreatures(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return false;
        for (Permanent p : battlefield) {
            if (p.isAttacking()) return true;
        }
        return false;
    }

    /**
     * Checks whether any creature on the defending player's battlefield
     * is currently blocking (i.e. blockers were declared this combat).
     */
    private boolean hasBlockingCreatures(GameData gameData) {
        UUID defenderId = gameQueryService.getOpponentId(gameData, gameData.activePlayerId);
        List<Permanent> defenderBattlefield = gameData.playerBattlefields.get(defenderId);
        if (defenderBattlefield == null) return false;
        for (Permanent p : defenderBattlefield) {
            if (p.isBlocking()) return true;
        }
        return false;
    }

    /**
     * Checks whether the given player controls any permanent with an
     * instant-speed activated ability that can currently be used.
     *
     * <p>Excludes mana abilities (CR 605.1a), loyalty abilities,
     * sorcery-speed abilities, upkeep-only abilities, and abilities
     * that require tapping when the permanent is already tapped.
     *
     * @param gameData the current game state
     * @param playerId the player to check
     * @return {@code true} if at least one usable instant-speed ability exists
     */
    boolean hasInstantSpeedActivatedAbility(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return false;

        for (Permanent perm : battlefield) {
            for (ActivatedAbility ability : perm.getCard().getActivatedAbilities()) {
                // Skip sorcery-speed and upkeep-only abilities
                if (ability.getTimingRestriction() == ActivationTimingRestriction.SORCERY_SPEED
                        || ability.getTimingRestriction() == ActivationTimingRestriction.ONLY_DURING_YOUR_UPKEEP) {
                    continue;
                }

                // Skip attack-only abilities when the permanent is not attacking
                if (ability.getTimingRestriction() == ActivationTimingRestriction.ONLY_WHILE_ATTACKING
                        && !perm.isAttacking()) {
                    continue;
                }

                // Skip mana abilities (any effect that produces mana makes the whole ability a mana ability per CR 605.1a)
                boolean isManaAbility = ability.getEffects().stream()
                        .anyMatch(e -> e instanceof ManaProducingEffect);
                if (isManaAbility) continue;

                // Skip loyalty abilities
                if (ability.getLoyaltyCost() != null) continue;

                // Skip if ability requires tap and permanent is tapped
                if (ability.isRequiresTap() && perm.isTapped()) continue;

                return true;
            }
        }
        return false;
    }
}
