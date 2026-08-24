package com.github.laxika.magicalvibes.service.turn;

import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.event.GameEventAudience;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.service.GameActionAvailabilityService;
import com.github.laxika.magicalvibes.service.StackResolutionService;
import com.github.laxika.magicalvibes.service.ability.AbilityActivationService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.combat.attack.CombatAttackService;
import com.github.laxika.magicalvibes.service.event.GameMutationCoordinator;
import com.github.laxika.magicalvibes.service.state.StateBasedActionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
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
    private final GameActionAvailabilityService actionAvailabilityService;
    private final TriggerCollectionService triggerCollectionService;
    private final StackResolutionService stackResolutionService;
    private final StepTriggerService stepTriggerService;
    private final CombatAttackService combatAttackService;
    private final GameMutationCoordinator mutationCoordinator;
    private final StateBasedActionService stateBasedActionService;

    public AutoPassService(
            GameQueryService gameQueryService,
            GameActionAvailabilityService actionAvailabilityService,
            TriggerCollectionService triggerCollectionService,
            StackResolutionService stackResolutionService,
            StepTriggerService stepTriggerService,
            CombatAttackService combatAttackService,
            GameMutationCoordinator mutationCoordinator,
            @Lazy StateBasedActionService stateBasedActionService) {
        this.gameQueryService = gameQueryService;
        this.actionAvailabilityService = actionAvailabilityService;
        this.triggerCollectionService = triggerCollectionService;
        this.stackResolutionService = stackResolutionService;
        this.stepTriggerService = stepTriggerService;
        this.combatAttackService = combatAttackService;
        this.mutationCoordinator = mutationCoordinator;
        this.stateBasedActionService = stateBasedActionService;
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
        if (gameData.interaction.isAwaitingInput()) {
            invalidateForAllPlayers(gameData);
            return;
        }

        // CR 117.5: State-based actions happen before triggered abilities are put on the stack
        // and before the next player receives priority. Keeping the check at this common
        // priority handoff covers special actions such as playing a land as well as casts,
        // activations, and explicit priority passes.
        stateBasedActionService.performStateBasedActions(gameData);
        if (gameData.status != GameStatus.RUNNING) return;

        // CR 603.3 / 117.5: Flush any mana-ability triggers that are still pending.
        // This ensures they reach the stack before auto-pass can skip past them.
        if (!gameData.pendingManaAbilityTriggers.isEmpty()) {
            gameData.stack.addAll(gameData.pendingManaAbilityTriggers);
            gameData.pendingManaAbilityTriggers.clear();
            gameData.priorityPassedBy.clear();
        }

        if (!gameData.interaction.isAwaitingInput()
                && gameData.hasPendingInteraction(PermanentChoiceContext.TriggeredModalTrigger.class)) {
            triggerCollectionService.processNextTriggeredModalTrigger(gameData);
        }

        // Process any pending spell-target triggers (e.g. Livewire Lash)
        if (!gameData.interaction.isAwaitingInput() && gameData.hasPendingInteraction(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class)) {
            triggerCollectionService.processNextSpellTargetTrigger(gameData);
        }

        if (!gameData.interaction.isAwaitingInput() && gameData.hasPendingInteraction(PermanentChoiceContext.ETBSpellTargetTrigger.class)) {
            triggerCollectionService.processNextETBSpellTargetTrigger(gameData);
        }

        // Process any pending multi-target triggers (ETB copies / ON_SELF_CAST up-to-N, e.g. Elder Deep-Fiend)
        if (!gameData.interaction.isAwaitingInput() && gameData.hasPendingInteraction(PermanentChoiceContext.ETBTokenMultiTargetTrigger.class)) {
            triggerCollectionService.processNextETBTokenMultiTargetTrigger(gameData);
        }

        // Process any pending spell-cast graveyard-target triggers (e.g. Teshar, Ancestor's Apostle)
        if (!gameData.interaction.isAwaitingInput() && gameData.hasPendingInteraction(PermanentChoiceContext.SpellGraveyardTargetTrigger.class)) {
            triggerCollectionService.processNextSpellGraveyardTargetTrigger(gameData);
        }

        // Process any pending discard self-triggers before death triggers
        if (!gameData.interaction.isAwaitingInput() && gameData.hasPendingInteraction(PermanentChoiceContext.DiscardTriggerAnyTarget.class)) {
            triggerCollectionService.processNextDiscardSelfTrigger(gameData);
        }

        if (!gameData.interaction.isAwaitingInput()
                && gameData.hasPendingInteraction(PermanentChoiceContext.PlotTriggerAnyTarget.class)) {
            triggerCollectionService.processNextPlotTrigger(gameData);
        }

        // Process any pending targeted controller-discard triggers (e.g. Zenith Seeker)
        if (!gameData.interaction.isAwaitingInput() && gameData.hasPendingInteraction(PermanentChoiceContext.DiscardControllerTriggerTarget.class)) {
            triggerCollectionService.processNextDiscardControllerTriggerTarget(gameData);
        }

        // Process any pending targeted attack triggers before death triggers
        if (!gameData.interaction.isAwaitingInput() && gameData.hasPendingInteraction(PermanentChoiceContext.AttackTriggerTarget.class)) {
            triggerCollectionService.processNextAttackTriggerTarget(gameData);
        }

        // Process any pending two-target attack counter-move triggers (Decimator Beetle)
        if (!gameData.interaction.isAwaitingInput() && gameData.hasPendingInteraction(PermanentChoiceContext.AttackCounterMoveFirstTarget.class)) {
            triggerCollectionService.processNextAttackCounterMoveFirstTarget(gameData);
        }

        // Process any pending targeted enter triggers (e.g. Reaper King's "destroy target permanent")
        if (!gameData.interaction.isAwaitingInput() && gameData.hasPendingInteraction(PermanentChoiceContext.EntersTriggerTarget.class)) {
            triggerCollectionService.processNextEntersTriggerTarget(gameData);
        }

        // Process any pending targeted death triggers before auto-passing
        if (!gameData.interaction.isAwaitingInput() && gameData.hasPendingInteraction(PermanentChoiceContext.DeathTriggerTarget.class)) {
            triggerCollectionService.processNextDeathTriggerTarget(gameData);
        }

        // Process any pending targeted leaves-the-battlefield triggers (e.g. Meadowboon)
        if (!gameData.interaction.isAwaitingInput() && gameData.hasPendingInteraction(PermanentChoiceContext.SelfTriggeredAbilityTarget.class)) {
            triggerCollectionService.processNextSelfTriggeredAbilityTarget(gameData);
        }

        // Process any pending explore targeted triggers
        if (!gameData.interaction.isAwaitingInput() && gameData.hasPendingInteraction(PermanentChoiceContext.ExploreTriggerTarget.class)) {
            triggerCollectionService.processNextExploreTriggerTarget(gameData);
        }
        // Process any pending exploit stack-target triggers
        if (!gameData.interaction.isAwaitingInput() && gameData.hasPendingInteraction(PermanentChoiceContext.ExploitTriggerTarget.class)) {
            triggerCollectionService.processNextExploitTriggerTarget(gameData);
        }

        // Process any pending clash targeted triggers
        if (!gameData.interaction.isAwaitingInput() && gameData.hasPendingInteraction(PermanentChoiceContext.ClashTriggerTarget.class)) {
            triggerCollectionService.processNextClashTriggerTarget(gameData);
        }

        // Process any pending life-gain targeted triggers
        if (!gameData.interaction.isAwaitingInput() && gameData.hasPendingInteraction(PermanentChoiceContext.LifeGainTriggerAnyTarget.class)) {
            triggerCollectionService.processNextLifeGainTriggerTarget(gameData);
        }

        // Process any pending draw targeted triggers (Niv-Mizzet, the Firemind)
        if (!gameData.interaction.isAwaitingInput() && gameData.hasPendingInteraction(PermanentChoiceContext.DrawTriggerAnyTarget.class)) {
            triggerCollectionService.processNextDrawTriggerTarget(gameData);
        }

        // Process any pending enters-from-graveyard targeted triggers
        if (!gameData.interaction.isAwaitingInput() && gameData.hasPendingInteraction(PermanentChoiceContext.EnteringPermanentAnyTargetTrigger.class)) {
            triggerCollectionService.processNextEnteringPermanentAnyTarget(gameData);
        }

        // Process any pending saga chapter targeted triggers
        if (!gameData.interaction.isAwaitingInput() && gameData.hasPendingInteraction(PermanentChoiceContext.SagaChapterTarget.class)) {
            triggerCollectionService.processNextSagaChapterTarget(gameData);
        }

        if (!gameData.interaction.isAwaitingInput()
                && gameData.hasPendingInteraction(PermanentChoiceContext.SagaChapterPlayerTarget.class)) {
            triggerCollectionService.processNextSagaChapterPlayerTarget(gameData);
        }

        // Process any pending end-step targeted triggers
        if (!gameData.interaction.isAwaitingInput() && gameData.hasPendingInteraction(PermanentChoiceContext.EndStepTriggerTarget.class)) {
            stepTriggerService.processNextEndStepTriggerTarget(gameData);
        }

        // Process any pending phase-in targeted triggers (queued during untap-step phasing)
        if (!gameData.interaction.isAwaitingInput() && gameData.hasPendingInteraction(PermanentChoiceContext.PhasesInTriggerTarget.class)) {
            stepTriggerService.processNextPhasesInTriggerTarget(gameData);
        }

        for (int safety = 0; safety < 100; safety++) {
            if (gameData.interaction.isAwaitingInput()) {
                invalidateForAllPlayers(gameData);
                return;
            }
            if (gameData.status == GameStatus.FINISHED) return;

            // When stack is non-empty, never auto-pass — players must explicitly pass
            if (!gameData.stack.isEmpty()) {
                invalidateForAllPlayers(gameData);
                return;
            }

            UUID priorityHolder = gameQueryService.getPriorityPlayerId(gameData);

            // If no one holds priority (both already passed), advance the step
            if (priorityHolder == null) {
                advanceStep.accept(gameData);
                continue;
            }

            boolean shouldStop = gameQueryService.withQueryScope(gameData,
                    () -> shouldStopForAvailableAction(gameData, priorityHolder));
            if (shouldStop) {
                invalidateForAllPlayers(gameData);
                return;
            }

            // Check if current step is in the priority holder's auto-stop set
            java.util.Set<TurnStep> stopSteps = gameData.playerAutoStopSteps.get(priorityHolder);
            if (stopSteps != null && stopSteps.contains(gameData.currentStep)) {
                invalidateForAllPlayers(gameData);
                return;
            }

            // Priority holder has nothing to play, or is a human with no auto-stop configured
            // for this step — auto-pass for them.
            String playerName = gameData.playerIdToName.get(priorityHolder);
            log.info("Game {} - Auto-passing priority for {} on step {}",
                    gameData.id, playerName, gameData.currentStep);

            gameData.priorityPassedBy.add(priorityHolder);

            if (gameData.priorityPassedBy.size() >= 2) {
                advanceStep.accept(gameData);
            }
            // When only one player auto-passed, skip the intermediate broadcast.
            // The next loop iteration will either broadcast (other player can act)
            // or auto-pass the other player too. All loop exit paths already
            // broadcast, so this avoids redundant view computation and reduces
            // lock contention for AI games.
        }

        // Safety: if we somehow looped 100 times, broadcast current state and stop
        log.warn("Game {} - resolveAutoPass hit safety limit", gameData.id);
        invalidateForAllPlayers(gameData);
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

            boolean canRespond = gameQueryService.withQueryScope(gameData, () -> {
                List<Integer> playable =
                        actionAvailabilityService.getPlayableCardIndices(gameData, stackPriorityHolder);
                boolean hasActivatable = hasInstantSpeedActivatedAbility(gameData, stackPriorityHolder);
                return !playable.isEmpty() || hasActivatable;
            });

            if (canRespond) {
                // Player can respond to the triggered ability — stop and let them
                invalidateForAllPlayers(gameData);
                return;
            }

            // Auto-pass for this player
            gameData.priorityPassedBy.add(stackPriorityHolder);
        }
    }

    /**
     * Decides whether a merely-playable card should halt auto-pass for the given priority holder.
     *
     * <p>AI-controlled players (and headless simulation, where every player is policy-driven)
     * must always be handed a priority window whenever they can act, so the AI gets a chance to
     * respond at instant speed. Human players, by contrast, only stop where they have explicitly
     * configured an auto-stop (handled later in the loop) — otherwise a single always-castable
     * card in hand (e.g. a free Phyrexian-mana instant like Mutagenic Growth, which
     * {@code getPlayableCardIndices} reports as playable every step) would defeat auto-pass and
     * force the player to manually pass at every phase.
     */
    private boolean shouldStopForPlayableCards(GameData gameData, UUID priorityHolder) {
        return gameData.simulation || hasPolicyDrivenPriority(gameData, priorityHolder);
    }

    private boolean shouldStopForAvailableAction(GameData gameData, UUID priorityHolder) {
        List<Integer> playable = actionAvailabilityService.getPlayableCardIndices(gameData, priorityHolder);
        if (!playable.isEmpty() && shouldStopForPlayableCards(gameData, priorityHolder)) {
            return true;
        }

        // The strict check above uses the floating mana pool, which a live AI player
        // almost never holds outside of a cast — so it would auto-pass an AI straight
        // through combat even when an instant plus untapped lands gives it a play
        // (e.g. pumping an unblocked attacker for lethal). Re-check against the
        // potential pool for live AI players. Headless simulation keeps the strict
        // behavior: rollouts don't enumerate mid-combat casts, and the extra
        // potential-pool build per priority window would slow MCTS for nothing.
        if (!gameData.simulation && hasPolicyDrivenPriority(gameData, priorityHolder)
                && !actionAvailabilityService.getPotentialPlayableCardIndices(
                        gameData, priorityHolder, List.of()).isEmpty()) {
            return true;
        }

        // After blockers are declared, stop for the attacking player so they can
        // respond to blocks (e.g. cast combat tricks or activate abilities).
        if (gameData.currentStep == TurnStep.DECLARE_BLOCKERS
                && priorityHolder.equals(gameData.activePlayerId)
                && hasBlockingCreatures(gameData)) {
            return true;
        }

        // Never auto-pass the active player through DECLARE_ATTACKERS when
        // an opponent's effect forces them to attack (e.g. Trove of Temptation)
        // and attackers have not yet been declared this combat.
        return gameData.currentStep == TurnStep.DECLARE_ATTACKERS
                && priorityHolder.equals(gameData.activePlayerId)
                && !hasAttackingCreatures(gameData, priorityHolder)
                && combatAttackService.isOpponentForcedToAttack(gameData, priorityHolder)
                && !combatAttackService.getAttackableCreatureIndices(gameData, priorityHolder).isEmpty();
    }

    /**
     * Whether this seat's priority windows are policy-driven rather than governed by a human's
     * configured auto-stops — an AI opponent, or a deterministic test that opted every seat in via
     * {@link GameData#alwaysOfferPriorityWindows} without claiming the seats are AI-controlled.
     */
    private boolean hasPolicyDrivenPriority(GameData gameData, UUID priorityHolder) {
        return gameData.aiPlayerIds.contains(priorityHolder) || gameData.alwaysOfferPriorityWindows;
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
                if ((ability.getTimingRestriction() == ActivationTimingRestriction.SORCERY_SPEED
                        && !gameQueryService.canActivateAbilityAtInstantSpeed(gameData, playerId, ability))
                        || ability.getTimingRestriction() == ActivationTimingRestriction.ONLY_DURING_YOUR_UPKEEP
                        || ability.getTimingRestriction() == ActivationTimingRestriction.ONLY_DURING_ANY_UPKEEP
                        || ability.getTimingRestriction() == ActivationTimingRestriction.ONLY_DURING_OPPONENTS_UPKEEP) {
                    continue;
                }

                // Skip attack-only abilities when the permanent is not attacking
                if (ability.getTimingRestriction() == ActivationTimingRestriction.ONLY_WHILE_ATTACKING
                        && !perm.isAttacking()) {
                    continue;
                }

                // Skip attack/block-only abilities when the permanent is doing neither
                if (ability.getTimingRestriction() == ActivationTimingRestriction.ONLY_WHILE_ATTACKING_OR_BLOCKING
                        && !perm.isAttacking() && !perm.isBlocking()) {
                    continue;
                }

                // Skip combat-only abilities when not in the combat phase
                if (ability.getTimingRestriction() == ActivationTimingRestriction.ONLY_DURING_COMBAT
                        && !gameData.currentStep.isCombatPhase()) {
                    continue;
                }

                // Skip end-of-combat-only abilities outside that step
                if (ability.getTimingRestriction() == ActivationTimingRestriction.ONLY_DURING_END_OF_COMBAT
                        && gameData.currentStep != TurnStep.END_OF_COMBAT) {
                    continue;
                }

                // Skip declare-blockers-only abilities outside that step
                if (ability.getTimingRestriction() == ActivationTimingRestriction.ONLY_DURING_DECLARE_BLOCKERS
                        && gameData.currentStep != TurnStep.DECLARE_BLOCKERS) {
                    continue;
                }

                // Skip declare-blockers-only abilities that also require the creature to be blocked
                if (ability.getTimingRestriction() == ActivationTimingRestriction.ONLY_DURING_DECLARE_BLOCKERS_IF_BLOCKED
                        && (gameData.currentStep != TurnStep.DECLARE_BLOCKERS
                                || !gameQueryService.isBlockedByAnyCreature(gameData, perm))) {
                    continue;
                }

                // Skip mana abilities using the shared CR 605.1a classifier.
                boolean isManaAbility = AbilityActivationService.isManaAbility(ability);
                if (isManaAbility) continue;

                // Skip loyalty abilities
                if (ability.getLoyaltyCost() != null) continue;

                // Skip if ability requires tap and permanent is tapped
                if (ability.isRequiresTap() && perm.isTapped()) continue;

                // Skip if ability requires untap ({Q}) and permanent is untapped
                if (ability.isRequiresUntap() && !perm.isTapped()) continue;

                return true;
            }
        }
        return false;
    }

    private void invalidateForAllPlayers(GameData gameData) {
        mutationCoordinator.emit(gameData,
                new GameEventFact.StateInvalidated(
                        GameEventFact.StateSection.PRIVATE_PLAYER_VIEW),
                GameEventAudience.allPlayers());
    }
}
