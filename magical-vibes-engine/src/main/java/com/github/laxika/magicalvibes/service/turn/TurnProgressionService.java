package com.github.laxika.magicalvibes.service.turn;
import com.github.laxika.magicalvibes.model.action.DelayedCombatDamageLoot;
import com.github.laxika.magicalvibes.model.action.DelayedCombatDamageReflection;
import com.github.laxika.magicalvibes.model.action.ExileAndReturnTransformedAtEndOfCombat;
import com.github.laxika.magicalvibes.model.action.DestroyEquipmentAtEndOfCombat;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.action.GainControlOfPermanentAtEndOfCombat;
import com.github.laxika.magicalvibes.model.action.PutCounterOnPermanentAtEndOfCombat;
import com.github.laxika.magicalvibes.model.action.RemoveCounterFromSourceAtEndOfCombat;
import com.github.laxika.magicalvibes.model.action.PutMinusOneCounterAtEndOfCombat;
import com.github.laxika.magicalvibes.model.action.SacrificeAtEndOfCombat;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.MakeTargetCopyOfTargetCreatureUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.combat.CombatResult;
import com.github.laxika.magicalvibes.service.combat.CombatService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TurnProgressionService {

    private final CombatService combatService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final TurnCleanupService turnCleanupService;
    private final UntapStepService untapStepService;
    private final StepTriggerService stepTriggerService;
    private final AutoPassService autoPassService;

    public void advanceStep(GameData gameData) {
        // The mana pool drains at step boundaries, so nothing recorded before this point can
        // still be reverted by the cancel-casting UI.
        gameData.revertableManaActivations.clear();

        // Process end-of-combat sacrifices, exiles, and equipment destruction when leaving END_OF_COMBAT
        if (gameData.currentStep == TurnStep.END_OF_COMBAT
                && (gameData.hasDelayedAction(SacrificeAtEndOfCombat.class)
                    || gameData.hasDelayedAction(DelayedPermanentAction.class,
                            a -> a.kind() == DelayedPermanentActionKind.EXILE_TOKEN_AT_END_OF_COMBAT
                                    || a.kind() == DelayedPermanentActionKind.DESTROY_AT_END_OF_COMBAT)
                    || gameData.hasDelayedAction(DestroyEquipmentAtEndOfCombat.class)
                    || gameData.hasDelayedAction(PutMinusOneCounterAtEndOfCombat.class)
                    || gameData.hasDelayedAction(PutCounterOnPermanentAtEndOfCombat.class)
                    || gameData.hasDelayedAction(RemoveCounterFromSourceAtEndOfCombat.class)
                    || gameData.hasDelayedAction(GainControlOfPermanentAtEndOfCombat.class)
                    || gameData.hasDelayedAction(ExileAndReturnTransformedAtEndOfCombat.class))) {
            combatService.processEndOfCombatSacrifices(gameData);
            combatService.processEndOfCombatExiles(gameData);
            combatService.processEndOfCombatEquipmentDestruction(gameData);
            combatService.processEndOfCombatDestructions(gameData);
            combatService.processEndOfCombatSourceCounters(gameData);
            combatService.processEndOfCombatOpponentCounters(gameData);
            combatService.processEndOfCombatCounterRemovals(gameData);
            combatService.processEndOfCombatControlGains(gameData);
            combatService.processEndOfCombatExileAndReturnTransformed(gameData);
            gameData.priorityPassedBy.clear();
            return;
        }

        gameData.priorityPassedBy.clear();
        gameData.interaction.clearAwaitingInput();

        if (gameData.currentStep == TurnStep.COMBAT_DAMAGE
                && gameData.combatDamageFirstStrikeStepComplete
                && !gameData.combatDamagePhase1Complete) {
            handleCombatResult(combatService.resolveCombatDamage(gameData), gameData);
            return;
        }

        TurnStep next = gameData.currentStep.next();

        // CR 508.8: If no creatures are attacking, skip declare blockers and combat damage
        if (gameData.currentStep == TurnStep.DECLARE_ATTACKERS) {
            List<Integer> attackers = combatService.getAttackingCreatureIndices(gameData, gameData.activePlayerId);
            if (attackers.isEmpty()) {
                next = TurnStep.END_OF_COMBAT;
            }
        }

        if (gameData.currentStep == TurnStep.POSTCOMBAT_MAIN && gameData.additionalCombatMainPhasePairs > 0) {
            next = TurnStep.BEGINNING_OF_COMBAT;
            gameData.additionalCombatMainPhasePairs--;
        }

        // Blinding Angel: the active player skips their next combat phase — jump straight from the
        // precombat main phase to the postcombat main phase.
        if (gameData.currentStep == TurnStep.PRECOMBAT_MAIN
                && gameData.skipNextCombatPhaseCount.getOrDefault(gameData.activePlayerId, 0) > 0) {
            next = TurnStep.POSTCOMBAT_MAIN;
            int remaining = gameData.skipNextCombatPhaseCount.get(gameData.activePlayerId) - 1;
            if (remaining > 0) {
                gameData.skipNextCombatPhaseCount.put(gameData.activePlayerId, remaining);
            } else {
                gameData.skipNextCombatPhaseCount.remove(gameData.activePlayerId);
            }
            String skipLog = gameData.playerIdToName.get(gameData.activePlayerId) + " skips their combat phase.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(skipLog));
        }

        turnCleanupService.drainManaPools(gameData);

        if (next != null) {
            gameData.currentStep = next;
            String logEntry = "Step: " + next.getDisplayName();
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - Step advanced to {}", gameData.id, next);
            gameBroadcastService.broadcastGameState(gameData);

            if (gameData.status == GameStatus.FINISHED) return;

            stepTriggerService.processPendingExileReturns(gameData, next);

            if (next == TurnStep.UPKEEP) {
                stepTriggerService.handleUpkeepTriggers(gameData);
            } else if (next == TurnStep.PRECOMBAT_MAIN) {
                stepTriggerService.handlePrecombatMainTriggers(gameData);
            } else if (next == TurnStep.DRAW) {
                stepTriggerService.handleDrawStep(gameData);
            } else if (next == TurnStep.BEGINNING_OF_COMBAT) {
                stepTriggerService.handleBeginningOfCombatTriggers(gameData);
            } else if (next == TurnStep.DECLARE_ATTACKERS) {
                combatService.handleDeclareAttackersStep(gameData);
            } else if (next == TurnStep.DECLARE_BLOCKERS) {
                handleCombatResult(combatService.handleDeclareBlockersStep(gameData), gameData);
            } else if (next == TurnStep.COMBAT_DAMAGE) {
                handleCombatResult(combatService.resolveCombatDamage(gameData), gameData);
            } else if (next == TurnStep.END_OF_COMBAT) {
                combatService.clearCombatState(gameData);
            } else if (next == TurnStep.END_STEP) {
                stepTriggerService.handleEndStepTriggers(gameData);
            } else if (next == TurnStep.CLEANUP) {
                // CR 514.1: Active player discards down to maximum hand size (normally 7)
                UUID activePlayerId = gameData.activePlayerId;
                List<Card> hand = gameData.playerHands.get(activePlayerId);
                int maxHandSize = Math.max(turnCleanupService.getMaxHandSize(gameData, activePlayerId), 0);
                if (hand != null && hand.size() > maxHandSize && !turnCleanupService.hasNoMaximumHandSize(gameData, activePlayerId)) {
                    int discardCount = hand.size() - maxHandSize;
                    gameData.cleanupDiscardPending = true;
                    gameData.discardCausedByOpponent = false;
                    playerInputService.beginDiscardChoice(gameData, activePlayerId, discardCount);
                    return;
                }
                // CR 514.2: Remove damage and end "until end of turn" effects
                turnCleanupService.applyCleanupResets(gameData);
            }
        } else {
            advanceTurn(gameData);
        }
    }

    void advanceTurn(GameData gameData) {
        // Clear any active mind control from the ending turn
        gameData.mindControlledPlayerId = null;
        gameData.mindControllerPlayerId = null;

        UUID nextActive;
        boolean skipUntapStep = false;
        if (!gameData.extraTurns.isEmpty()) {
            nextActive = gameData.extraTurns.pollFirst();
            skipUntapStep = Boolean.TRUE.equals(gameData.extraTurnSkipsUntap.pollFirst());
        } else {
            List<UUID> ids = new ArrayList<>(gameData.orderedPlayerIds);
            UUID currentActive = gameData.activePlayerId;
            nextActive = ids.get(0).equals(currentActive) ? ids.get(1) : ids.get(0);
        }
        String nextActiveName = gameData.playerIdToName.get(nextActive);

        gameData.activePlayerId = nextActive;

        // Check for pending Taunt on the new active player: promote it to an active this-turn requirement
        gameData.tauntedThisTurn.clear();
        UUID taunter = gameData.tauntedNextTurn.remove(nextActive);
        if (taunter != null && gameData.playerIds.contains(taunter)) {
            gameData.tauntedThisTurn.put(nextActive, taunter);
            String taunterName = gameData.playerIdToName.get(taunter);
            String tauntLog = "Creatures " + nextActiveName + " controls must attack " + taunterName + " this turn if able.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(tauntLog));
            log.info("Game {} - {}'s creatures must attack {} this turn (Taunt)", gameData.id, nextActiveName, taunterName);
        }

        // Check for pending Mindslaver control on the new active player
        UUID pendingController = gameData.pendingTurnControl.remove(nextActive);
        if (pendingController != null && gameData.playerIds.contains(pendingController)) {
            gameData.mindControlledPlayerId = nextActive;
            gameData.mindControllerPlayerId = pendingController;
            String controllerName = gameData.playerIdToName.get(pendingController);
            String controlLog = controllerName + " controls " + nextActiveName + " this turn (Mindslaver).";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(controlLog));
            log.info("Game {} - {} controls {} this turn (Mindslaver)", gameData.id, controllerName, nextActiveName);
        }
        gameData.turnNumber++;
        gameData.currentStep = TurnStep.first();
        gameData.interaction.clearAwaitingInput();
        gameData.priorityPassedBy.clear();
        gameData.landsPlayedThisTurn.clear();
        gameData.additionalLandsThisTurn.clear();
        gameData.permanentsEnteredBattlefieldThisTurn.clear();
        gameData.snapshotSpellCountsAndClear(gameData.spellsCastLastTurn);
        gameData.permanentTypesCastFromGraveyardThisTurn.clear();
        gameData.playersDeclaredAttackersThisTurn.clear();
        gameData.creaturesAttackedCountThisTurn.clear();
        gameData.playersSilencedThisTurn.clear();
        gameData.activatedAbilityUsesThisTurn.clear();
        gameData.permanentAbilityResolutionsThisTurn.clear();
        gameData.creatureCardsPutIntoGraveyardFromBattlefieldThisTurn.clear();
        gameData.cardsPutIntoGraveyardFromBattlefieldThisTurn.clear();
        gameData.cardsPutIntoGraveyardFromAnywhereThisTurn.clear();
        gameData.creatureDeathCountThisTurn.clear();
        gameData.cardsDrawnThisTurn.clear();
        gameData.cardsDrawnThisTurnIds.clear();
        gameData.cardsDiscardedThisTurn.clear();
        gameData.lifeGainedThisTurn.clear();
        gameData.lifeLostThisTurn.clear();
        gameData.combatDamageToPlayersThisTurn.clear();
        gameData.noncombatDamageToPlayersThisTurn.clear();
        gameData.clearDelayedActions(DelayedCombatDamageLoot.class);
        gameData.clearDelayedActions(DelayedCombatDamageReflection.class);
        gameData.combatDamageSourceSubtypesThisTurn.clear();
        gameData.combatDamageSourcesWithChangelingThisTurn.clear();
        gameData.combatDamageToPlayerControllerSubtypesThisTurn.clear();
        gameData.controllersDealtCombatDamageWithChangelingThisTurn.clear();
        gameData.playersDealtDamageThisTurn.clear();
        gameData.damageDealtToPlayersThisTurn.clear();
        gameData.untappedLandsAtTurnStart.clear();
        gameData.permanentsDealtDamageThisTurn.clear();
        gameData.creatureCardsDamagedThisTurnBySourcePermanent.clear();
        gameData.creatureGivingControllerPoisonOnDeathThisTurn.clear();
        gameData.creaturesReturnedToBattlefieldOnDeathThisTurn.clear();
        gameData.creatureCreatingTokenOnDeathThisTurn.clear();
        gameData.additionalCombatMainPhasePairs = 0;
        gameData.cleanupDiscardPending = false;
        gameData.paidSearchTaxPermanentIds.clear();

        turnCleanupService.drainManaPools(gameData);

        gameData.forEachPermanent((playerId, p) -> p.setAttackedThisTurn(false));

        // Clear "until your next turn" activated abilities for the active player's permanents
        List<Permanent> activePlayerBf = gameData.playerBattlefields.get(nextActive);
        if (activePlayerBf != null) {
            activePlayerBf.forEach(Permanent::clearUntilNextTurnEffects);
            // Promote/expire Wall of Dust "can't attack next turn" restrictions on the active player's
            // creatures (scoped to their controller's turn so it never arms on an opponent's turn).
            activePlayerBf.forEach(Permanent::promoteCantAttackNextTurn);
        }
        // "Until your next turn" floating continuous effects controlled by the player whose turn
        // is beginning wear off now. An expiring layer-1 copy effect (e.g. Shapesharer) reverts
        // the copied permanent's card — which may sit on any player's battlefield. A newer copy
        // effect overwrites {@code copyUntilNextTurnControllerId}, so an older effect expiring
        // first must not revert the card out from under the still-active newer one.
        for (FloatingContinuousEffect expired : gameData.expireFloatingEffectsAtTurnStart(nextActive)) {
            if (expired.effect() instanceof MakeTargetCopyOfTargetCreatureUntilNextTurnEffect
                    && expired.affectedPermanentId() != null) {
                Permanent copy = findPermanent(gameData, expired.affectedPermanentId());
                if (copy != null && copy.isCopyUntilControllerNextTurn()
                        && nextActive.equals(copy.getCopyUntilNextTurnControllerId())) {
                    copy.revertUntilNextTurnCopy();
                }
            }
        }

        if (skipUntapStep) {
            // Savor the Moment: this extra turn skips its untap step (nothing untaps, no Storage
            // Matrix choice), but summoning sickness still clears.
            untapStepService.untapPermanents(gameData, nextActive, null, true);
        } else {
            // Storage Matrix: pause the untap step so the active player chooses artifact/creature/land
            // before untapping. The choice handler resumes via resumeStorageMatrixUntap.
            if (untapStepService.storageMatrixRestrictionApplies(gameData, nextActive)) {
                playerInputService.beginStorageMatrixUntapChoice(gameData, nextActive);
                gameBroadcastService.broadcastGameState(gameData);
                return;
            }

            // Static Orb / Stoic Angel: pause the untap step so the active player chooses up to the
            // cap of the matching permanents to untap. The choice handler resumes via
            // resumeStaticOrbUntap.
            java.util.Optional<com.github.laxika.magicalvibes.model.effect.StaticOrbEffect> untapRestriction =
                    untapStepService.bindingUntapRestriction(gameData, nextActive);
            if (untapRestriction.isPresent()) {
                com.github.laxika.magicalvibes.model.effect.StaticOrbEffect effect = untapRestriction.get();
                playerInputService.beginStaticOrbUntapChoice(gameData, nextActive,
                        untapStepService.staticOrbUntapCandidates(gameData, nextActive, effect),
                        effect.maxUntap(), effect.filter());
                gameBroadcastService.broadcastGameState(gameData);
                return;
            }

            untapStepService.untapPermanents(gameData, nextActive);
        }

        // Process pending may-not-untap choices before continuing turn
        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
            return;
        }

        completeTurnAdvance(gameData);
    }

    /**
     * Resumes the paused untap step after the active player answers a Storage Matrix type choice.
     * Only permanents matching {@code restrictPredicate} untap; the rest of the untap-step
     * bookkeeping and turn advance then proceeds exactly as {@link #advanceTurn} would have.
     */
    public void resumeStorageMatrixUntap(GameData gameData, UUID activePlayerId,
                                         com.github.laxika.magicalvibes.model.filter.PermanentPredicate restrictPredicate) {
        untapStepService.untapPermanents(gameData, activePlayerId, restrictPredicate);

        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
            return;
        }

        completeTurnAdvance(gameData);
    }

    /**
     * Resumes the paused untap step after the active player picks which capped permanents untap
     * under an untap-cap restriction (Static Orb / Smoke). Only the chosen permanents untap among
     * those matching {@code capFilter}; permanents outside the cap filter untap normally. The
     * remaining untap-step bookkeeping and turn advance then proceed exactly as {@link #advanceTurn}
     * would have.
     */
    public void resumeStaticOrbUntap(GameData gameData, UUID activePlayerId,
                                     java.util.Set<UUID> chosenUntapIds,
                                     com.github.laxika.magicalvibes.model.filter.PermanentPredicate staticOrbFilter) {
        untapStepService.untapChosenPermanents(gameData, activePlayerId, chosenUntapIds, staticOrbFilter);

        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
            return;
        }

        completeTurnAdvance(gameData);
    }

    private Permanent findPermanent(GameData gameData, UUID permanentId) {
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent p : bf) {
                if (p.getId().equals(permanentId)) {
                    return p;
                }
            }
        }
        return null;
    }

    /**
     * Called after all may-not-untap choices have been resolved to finish the turn advance
     * (log the turn start and broadcast game state).
     */
    public void completeTurnAdvance(GameData gameData) {
        String activeName = gameData.playerIdToName.get(gameData.activePlayerId);
        String logEntry = "Turn " + gameData.turnNumber + " begins. " + activeName + "'s turn.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        log.info("Game {} - Turn {} begins. Active player: {}", gameData.id, gameData.turnNumber, activeName);
        gameBroadcastService.broadcastGameState(gameData);
    }

    public void handleCombatResult(CombatResult result, GameData gameData) {
        if (result == CombatResult.ADVANCE_AND_AUTO_PASS || result == CombatResult.ADVANCE_ONLY) {
            advanceStep(gameData);
        }
        if (result == CombatResult.AUTO_PASS_RESOLVE_COMBAT_TRIGGERS) {
            autoPassService.resolveAutoPassCombatTriggers(gameData);
        }
        if (result == CombatResult.ADVANCE_AND_AUTO_PASS || result == CombatResult.AUTO_PASS_ONLY
                || result == CombatResult.AUTO_PASS_RESOLVE_COMBAT_TRIGGERS) {
            resolveAutoPass(gameData);
        }
    }

    public void resolveAutoPass(GameData gameData) {
        // Process pending may abilities before auto-passing (e.g. attack-triggered "you may" effects)
        // Only when the stack is empty — otherwise stack items (e.g. Time Stop) must resolve first
        if (gameData.stack.isEmpty() && !gameData.pendingMayAbilities.isEmpty()
                && !gameData.interaction.isAwaitingInput()) {
            playerInputService.processNextMayAbility(gameData);
            return;
        }
        autoPassService.resolveAutoPass(gameData, this::advanceStep);
    }

    public void applyCleanupResets(GameData gameData) {
        turnCleanupService.applyCleanupResets(gameData);
    }

    public void processNextUpkeepAnyTargetTrigger(GameData gameData) {
        stepTriggerService.processNextUpkeepAnyTargetTrigger(gameData);
    }

    public void processNextUpkeepPermanentTarget(GameData gameData) {
        stepTriggerService.processNextUpkeepPermanentTarget(gameData);
    }

    public void processNextUpkeepPlayerTarget(GameData gameData) {
        stepTriggerService.processNextUpkeepPlayerTarget(gameData);
    }

    public void processNextUpkeepMultiPlayerTarget(GameData gameData) {
        stepTriggerService.processNextUpkeepMultiPlayerTarget(gameData);
    }

    public void processUpkeepSecondPlayerTarget(GameData gameData, PermanentChoiceContext.UpkeepSecondPlayerTargetTrigger trigger) {
        stepTriggerService.processUpkeepSecondPlayerTarget(gameData, trigger);
    }

    public void processNextUpkeepCopyTarget(GameData gameData) {
        stepTriggerService.processNextUpkeepCopyTarget(gameData);
    }

    public void processNextCapriciousEfreetTarget(GameData gameData) {
        stepTriggerService.processNextCapriciousEfreetTarget(gameData);
    }

    public void processNextPucasMischiefTarget(GameData gameData) {
        stepTriggerService.processNextPucasMischiefTarget(gameData);
    }

    public void processNextEndStepTriggerTarget(GameData gameData) {
        stepTriggerService.processNextEndStepTriggerTarget(gameData);
    }

    public void processNextBeginningOfCombatTriggerTarget(GameData gameData) {
        stepTriggerService.processNextBeginningOfCombatTriggerTarget(gameData);
    }
}
