package com.github.laxika.magicalvibes.service.turn;
import com.github.laxika.magicalvibes.model.action.AddManaAtNextMainPhase;
import com.github.laxika.magicalvibes.model.action.DelayedAdditionalCombatBeginningEffect;
import com.github.laxika.magicalvibes.model.action.TargetCreatureMustAttackNextCombat;
import com.github.laxika.magicalvibes.model.action.DelayedBeginningOfCombatTrigger;
import com.github.laxika.magicalvibes.model.action.DelayedCombatDamageLoot;
import com.github.laxika.magicalvibes.model.action.DelayedCombatDamageToken;
import com.github.laxika.magicalvibes.model.action.DelayedCombatDamageDraw;
import com.github.laxika.magicalvibes.model.action.DelayedCombatDamageLookAtHandAndDraw;
import com.github.laxika.magicalvibes.model.action.DelayedCombatDamageReflection;
import com.github.laxika.magicalvibes.model.action.DelayedBlockerBoost;
import com.github.laxika.magicalvibes.model.action.DelayedAttackerBoost;
import com.github.laxika.magicalvibes.model.action.DelayedAttackerKeywordGrant;
import com.github.laxika.magicalvibes.model.action.DelayedAttackUntap;
import com.github.laxika.magicalvibes.model.action.DelayedAttackTokenCreation;
import com.github.laxika.magicalvibes.model.action.DelayedAttackDamage;
import com.github.laxika.magicalvibes.model.action.DelayedVehicleAttack;
import com.github.laxika.magicalvibes.model.action.DelayedNontokenAttackTokenCreation;
import com.github.laxika.magicalvibes.model.action.DelayedOpponentAttackerBoost;
import com.github.laxika.magicalvibes.model.action.DelayedDestroyCreatureDealingCombatDamageToPlaneswalker;
import com.github.laxika.magicalvibes.model.action.DelayedWatchedCreaturesCombatDamage;
import com.github.laxika.magicalvibes.model.action.DelayedDamageDoubling;
import com.github.laxika.magicalvibes.model.action.DelayedControllerSpellCastTrigger;
import com.github.laxika.magicalvibes.model.action.DelayedUnblockedAttackerCubeCounter;
import com.github.laxika.magicalvibes.model.action.DelayedUnblockedAttackerGainLife;
import com.github.laxika.magicalvibes.model.action.DelayedUnblockedAttackerPowerDamage;
import com.github.laxika.magicalvibes.model.action.DelayedDestroyCreatureDamagedByWatchedCreature;
import com.github.laxika.magicalvibes.model.action.DelayedSacrificeSourceWhenTargetLeaves;
import com.github.laxika.magicalvibes.model.action.DelayedSacrificeTargetWhenSourceLeaves;
import com.github.laxika.magicalvibes.model.action.DelayedDestroyTargetWhenSourceLeaves;
import com.github.laxika.magicalvibes.model.action.ExileAndReturnTransformedAtEndOfCombat;
import com.github.laxika.magicalvibes.model.action.DealDamageToPermanentAtEndOfCombat;
import com.github.laxika.magicalvibes.model.action.DestroyCombatOpponentsAtEndOfCombat;
import com.github.laxika.magicalvibes.model.action.DestroyCombatOpponentAtEndOfCombatThenPutCounterOnSource;
import com.github.laxika.magicalvibes.model.action.DestroyEquipmentAtEndOfCombat;
import com.github.laxika.magicalvibes.model.action.DestroyPermanentIfDidNotAttackAtEndStep;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.action.GainControlOfPermanentAtEndOfCombat;
import com.github.laxika.magicalvibes.model.action.LoseGameAtEndStep;
import com.github.laxika.magicalvibes.model.action.PhaseOutAtEndOfCombat;
import com.github.laxika.magicalvibes.model.action.PutCounterOnPermanentAtEndOfCombat;
import com.github.laxika.magicalvibes.model.action.RemoveCounterFromSourceAtEndOfCombat;
import com.github.laxika.magicalvibes.model.action.PutMinusOneCounterAtEndOfCombat;
import com.github.laxika.magicalvibes.model.action.SacrificeAtEndOfCombat;
import com.github.laxika.magicalvibes.model.action.TapAndSkipUntapAtEndOfCombat;
import com.github.laxika.magicalvibes.model.action.TapCombatOpponentsAtEndOfCombat;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.MakeTargetCopyOfTargetCreatureUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ExtraTurnSkipReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.TimeVaultReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.SkipStepOrPhaseKind;
import com.github.laxika.magicalvibes.model.event.GameEventAudience;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.combat.CombatResult;
import com.github.laxika.magicalvibes.service.combat.CombatService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.event.GameMutationCoordinator;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class TurnProgressionService {

    private final CombatService combatService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final TurnCleanupService turnCleanupService;
    private final UntapStepService untapStepService;
    private final StepTriggerService stepTriggerService;
    private final AutoPassService autoPassService;
    private final GameMutationCoordinator mutationCoordinator;
    private final GameQueryService gameQueryService;

    @Autowired
    public TurnProgressionService(CombatService combatService, GameLogService gameLogService,
                                  PlayerInputService playerInputService, TurnCleanupService turnCleanupService,
                                  UntapStepService untapStepService, StepTriggerService stepTriggerService,
                                  AutoPassService autoPassService, GameMutationCoordinator mutationCoordinator,
                                  GameQueryService gameQueryService) {
        this.combatService = combatService;
        this.gameLogService = gameLogService;
        this.playerInputService = playerInputService;
        this.turnCleanupService = turnCleanupService;
        this.untapStepService = untapStepService;
        this.stepTriggerService = stepTriggerService;
        this.autoPassService = autoPassService;
        this.mutationCoordinator = mutationCoordinator;
        this.gameQueryService = gameQueryService;
    }

    public TurnProgressionService(CombatService combatService, GameLogService gameLogService,
                                  PlayerInputService playerInputService, TurnCleanupService turnCleanupService,
                                  UntapStepService untapStepService, StepTriggerService stepTriggerService,
                                  AutoPassService autoPassService, GameMutationCoordinator mutationCoordinator) {
        this(combatService, gameLogService, playerInputService, turnCleanupService, untapStepService,
                stepTriggerService, autoPassService, mutationCoordinator, null);
    }

    public void advanceStep(GameData gameData) {
        // The mana pool drains at step boundaries, so nothing recorded before this point can
        // still be reverted by the cancel-casting UI.
        gameData.revertableManaActivations.clear();

        // Process end-of-combat sacrifices, exiles, and equipment destruction when leaving END_OF_COMBAT
        if (gameData.currentStep == TurnStep.END_OF_COMBAT && hasEndOfCombatActions(gameData)) {
            processEndOfCombatActions(gameData);
            gameData.priorityPassedBy.clear();
            return;
        }

        if (gameData.currentStep == TurnStep.END_OF_COMBAT && gameData.mindControlUntilEndOfCombat) {
            gameData.mindControlledPlayerId = null;
            gameData.mindControllerPlayerId = null;
            gameData.mindControlUntilEndOfCombat = false;
        }

        if (gameData.currentStep == TurnStep.END_OF_COMBAT) {
            combatService.clearCombatState(gameData);
            gameData.expireEndOfCombatFloatingEffects();
            gameData.creaturesWithCombatDamagePreventedThisCombat.clear();
            gameData.creaturesPreventedFromDealingCombatDamageThisCombat.clear();
            gameData.onlyLandCreaturesCanAttackThisCombat = false;
            gameData.onlyAggressiveCreaturesCanAttackThisCombat = false;
            gameData.onlyPermanentCanAttackThisCombatId = null;
            gameData.playerManaPools.values().forEach(manaPool -> manaPool.clearCombatMana());
        }

        gameData.priorityPassedBy.clear();
        gameData.interaction.clearAwaitingInput();

        if (gameData.currentStep == TurnStep.COMBAT_DAMAGE
                && (gameData.combatDamageFirstestStrikeStepComplete
                || gameData.combatDamageFirstStrikeStepComplete)
                && !gameData.combatDamagePhase1Complete) {
            handleCombatResult(combatService.resolveCombatDamage(gameData), gameData);
            return;
        }

        TurnStep next = gameData.currentStep.next();
        boolean nextUpkeepIsAdditional = false;
        boolean additionalBeginningPhaseUpkeep = gameData.currentStep == TurnStep.UNTAP
                && gameData.additionalBeginningPhaseUntapInProgress;

        if (gameData.currentStep == TurnStep.UPKEEP && gameData.additionalUpkeepsRemaining > 0) {
            next = TurnStep.UPKEEP;
            gameData.additionalUpkeepsRemaining--;
            nextUpkeepIsAdditional = true;
        }

        boolean additionalCombatPhase = false;

        if (next == TurnStep.UPKEEP && stepTriggerService.playersSkipUpkeepStepApplies(gameData)) {
            if (nextUpkeepIsAdditional) {
                while (gameData.additionalUpkeepsRemaining > 0) {
                    gameData.additionalUpkeepsRemaining--;
                }
                next = TurnStep.DRAW;
            } else if (gameData.currentStep == TurnStep.UNTAP) {
                next = additionalBeginningPhaseUpkeep ? TurnStep.DRAW : TurnStep.PRECOMBAT_MAIN;
            }
            logSkippedPhase(gameData, "upkeep");
        }

        // CR 508.8: If no creatures are attacking, skip declare blockers and combat damage
        if (gameData.currentStep == TurnStep.DECLARE_ATTACKERS) {
            List<Integer> attackers = combatService.getAttackingCreatureIndices(gameData, gameData.activePlayerId);
            if (attackers.isEmpty()) {
                next = TurnStep.END_OF_COMBAT;
            }
        }

        if ((gameData.currentStep == TurnStep.PRECOMBAT_MAIN
                || gameData.currentStep == TurnStep.POSTCOMBAT_MAIN)
                && gameData.additionalCombatMainPhasePairs > 0) {
            next = TurnStep.BEGINNING_OF_COMBAT;
            gameData.additionalCombatMainPhasePairs--;
            additionalCombatPhase = true;
        } else if (gameData.currentStep == TurnStep.POSTCOMBAT_MAIN
                && gameData.additionalCombatMainPhasePairsReturnStep != null) {
            next = gameData.additionalCombatMainPhasePairsReturnStep;
            gameData.additionalCombatMainPhasePairsReturnStep = null;
        }

        if ((gameData.currentStep == TurnStep.PRECOMBAT_MAIN
                || gameData.currentStep == TurnStep.POSTCOMBAT_MAIN)
                && gameData.additionalCombatPhasesAfterMain > 0) {
            next = TurnStep.BEGINNING_OF_COMBAT;
            gameData.additionalCombatPhasesAfterMain--;
        }

        // Finest Hour: an additional combat phase with no additional main phase — loop straight from
        // this combat's end back into another combat phase (skipping the postcombat main phase).
        if (gameData.currentStep == TurnStep.END_OF_COMBAT
                && gameData.combatPhasesThisTurn == 1
                && controlsAggressiveCreature(gameData, gameData.activePlayerId)) {
            gameData.additionalCombatPhasesOnly++;
            gameData.aggressiveCombatPhasesOnly++;
        }

        if (gameData.currentStep == TurnStep.END_OF_COMBAT && gameData.additionalCombatPhasesOnly > 0) {
            next = TurnStep.BEGINNING_OF_COMBAT;
            gameData.additionalCombatPhasesOnly--;
            if (gameData.aggressiveCombatPhasesOnly > 0) {
                gameData.aggressiveCombatPhasesOnly--;
                gameData.onlyAggressiveCreaturesCanAttackThisCombat = true;
            }
            additionalCombatPhase = true;
        }

        if (gameData.currentStep == TurnStep.END_OF_COMBAT
                && gameData.additionalCombatPhasesOnly == 0
                && gameData.additionalCombatPhasesAfterMain > 0) {
            next = TurnStep.BEGINNING_OF_COMBAT;
            gameData.additionalCombatPhasesAfterMain--;
        } else if (gameData.currentStep == TurnStep.END_OF_COMBAT
                && gameData.additionalCombatPhasesAfterMain == 0
                && gameData.additionalCombatPhasesAfterMainReturnStep != null) {
            next = gameData.additionalCombatPhasesAfterMainReturnStep;
            gameData.additionalCombatPhasesAfterMainReturnStep = null;
        }

        if (gameData.currentStep == TurnStep.END_STEP && gameData.additionalEndStepsPending > 0) {
            next = TurnStep.END_STEP;
            gameData.additionalEndStepsPending--;
        }

        if (gameData.currentStep == TurnStep.END_OF_COMBAT
                && gameData.additionalUpkeepStepsAfterCombat > 0) {
            gameData.additionalUpkeepReturnStep = next;
            if (stepTriggerService.playersSkipUpkeepStepApplies(gameData)) {
                gameData.additionalUpkeepStepsAfterCombat = 0;
                gameData.additionalUpkeepReturnStep = null;
                logSkippedPhase(gameData, "additional upkeep steps");
            } else {
                next = TurnStep.UPKEEP;
                gameData.additionalUpkeepStepsAfterCombat--;
            }
        } else if (gameData.currentStep == TurnStep.UPKEEP
                && gameData.additionalUpkeepReturnStep != null) {
            if (gameData.additionalUpkeepStepsAfterCombat > 0
                    && stepTriggerService.playersSkipUpkeepStepApplies(gameData)) {
                gameData.additionalUpkeepStepsAfterCombat = 0;
                next = gameData.additionalUpkeepReturnStep;
                gameData.additionalUpkeepReturnStep = null;
                logSkippedPhase(gameData, "remaining additional upkeep steps");
            } else if (gameData.additionalUpkeepStepsAfterCombat > 0) {
                next = TurnStep.UPKEEP;
                gameData.additionalUpkeepStepsAfterCombat--;
            } else {
                next = gameData.additionalUpkeepReturnStep;
                gameData.additionalUpkeepReturnStep = null;
            }
        }

        if (gameData.currentStep == TurnStep.END_OF_COMBAT
                && gameData.additionalBeginningPhasesAfterCombat > 0) {
            gameData.additionalBeginningPhaseReturnStep = next;
            next = TurnStep.UNTAP;
            gameData.additionalBeginningPhasesAfterCombat--;
        } else if (gameData.currentStep == TurnStep.DRAW
                && gameData.additionalBeginningPhaseReturnStep != null) {
            if (gameData.additionalBeginningPhasesAfterCombat > 0) {
                next = TurnStep.UNTAP;
                gameData.additionalBeginningPhasesAfterCombat--;
            } else {
                next = gameData.additionalBeginningPhaseReturnStep;
                gameData.additionalBeginningPhaseReturnStep = null;
            }
        }

        // Blinding Angel: the active player skips their next combat phase — jump straight from the
        // precombat main phase to the postcombat main phase.
        if (gameData.currentStep == TurnStep.PRECOMBAT_MAIN
                && gameData.skipNextCombatPhaseCount.getOrDefault(gameData.activePlayerId, 0) > 0) {
            next = TurnStep.POSTCOMBAT_MAIN;
            gameData.skipCombatPhaseExpirationsThisTurn.computeIfPresent(gameData.activePlayerId,
                    (playerId, count) -> count > 1 ? count - 1 : null);
            int remaining = gameData.skipNextCombatPhaseCount.get(gameData.activePlayerId) - 1;
            if (remaining > 0) {
                gameData.skipNextCombatPhaseCount.put(gameData.activePlayerId, remaining);
            } else {
                gameData.skipNextCombatPhaseCount.remove(gameData.activePlayerId);
            }
            String skipLog = gameData.playerIdToName.get(gameData.activePlayerId) + " skips their combat phase.";
            gameLogService.append(gameData, GameLog.text(skipLog));
        }

        next = skipChosenPhases(gameData, next);

        // An additional combat created by a Throat Wolf is controlled by that Wolf's controller,
        // not necessarily by the player whose combat just ended. Restore the original active
        // player after the inserted combat has finished.
        if (next == TurnStep.BEGINNING_OF_COMBAT && additionalCombatPhase) {
            DelayedAdditionalCombatBeginningEffect pending =
                    gameData.peekFirstDelayedAction(DelayedAdditionalCombatBeginningEffect.class);
            if (pending != null && pending.controllerId() != null
                    && !pending.controllerId().equals(gameData.activePlayerId)) {
                if (gameData.additionalCombatReturnActivePlayerId == null) {
                    gameData.additionalCombatReturnActivePlayerId = gameData.activePlayerId;
                }
                gameData.activePlayerId = pending.controllerId();
            }
        } else if (gameData.currentStep == TurnStep.END_OF_COMBAT
                && gameData.additionalCombatReturnActivePlayerId != null) {
            gameData.activePlayerId = gameData.additionalCombatReturnActivePlayerId;
            gameData.additionalCombatReturnActivePlayerId = null;
        }

        if (next == TurnStep.END_STEP) {
            gameData.endStepsThisTurn++;
        }

        turnCleanupService.drainManaPools(gameData);

        if (gameData.currentStep == TurnStep.UPKEEP) {
            gameData.expireFloatingEffectsAtUpkeepEnd(gameData.activePlayerId);
        }

        if (next != null) {
            gameData.currentStep = next;
            gameData.currentUpkeepIsAdditional = next == TurnStep.UPKEEP
                    && (nextUpkeepIsAdditional || additionalBeginningPhaseUpkeep);
            if (next == TurnStep.UPKEEP) {
                gameData.markUpkeepStart();
            }
            String logEntry = "Step: " + next.getDisplayName();
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - Step advanced to {}", gameData.id, next);
            invalidateForAllPlayers(gameData);

            if (gameData.status == GameStatus.FINISHED) return;

            if (next == TurnStep.UNTAP) {
                beginAdditionalBeginningPhase(gameData);
                return;
            }

            stepTriggerService.processPendingExileReturns(gameData, next);

            if (gameData.interaction.isAwaitingInput()) {
                return;
            }

            if (next == TurnStep.UPKEEP) {
                stepTriggerService.handleUpkeepTriggers(gameData);
            } else if (next == TurnStep.PRECOMBAT_MAIN) {
                stepTriggerService.handlePrecombatMainTriggers(gameData);
            } else if (next == TurnStep.POSTCOMBAT_MAIN) {
                stepTriggerService.handlePostcombatMainTriggers(gameData);
                // Conduit of Storms / Emrakul: "add mana at the beginning of your next main phase this turn"
                // after attacking — the next main is postcombat.
                stepTriggerService.drainAddManaAtNextMainPhase(gameData, false);
            } else if (next == TurnStep.DRAW) {
                stepTriggerService.handleDrawStep(gameData);
            } else if (next == TurnStep.BEGINNING_OF_COMBAT) {
                gameData.combatPhasesThisTurn++;
                UUID combatController = gameData.pendingCombatControl.remove(gameData.activePlayerId);
                if (combatController != null && gameData.playerIds.contains(combatController)) {
                    gameData.mindControlledPlayerId = gameData.activePlayerId;
                    gameData.mindControllerPlayerId = combatController;
                    gameData.mindControlUntilEndOfCombat = true;
                    String combatControllerName = gameData.playerIdToName.get(combatController);
                    String combatPlayerName = gameData.playerIdToName.get(gameData.activePlayerId);
                    String controlLog = combatControllerName + " controls " + combatPlayerName
                            + " during their next combat phase.";
                    gameLogService.append(gameData, GameLog.text(controlLog));
                    log.info("Game {} - {} controls {} during combat", gameData.id,
                            combatControllerName, combatPlayerName);
                }
                gameData.cardsPutIntoGraveyardThisCombat.clear();
                gameData.declaredAttackerIdsThisCombat.clear();
                gameData.forEachPermanent((playerId, p) -> {
                    p.setAttackedThisCombat(false);
                    p.setBlockedThisCombat(false);
                });
                gameData.combatBlockOpponentIdsThisCombat.clear();
                gameData.combatDamageToPlayersThisCombat.clear();
                processTargetCreatureMustAttackNextCombat(gameData);
                if (additionalCombatPhase) {
                    processAdditionalCombatBeginningEffects(gameData);
                }
                stepTriggerService.handleBeginningOfCombatTriggers(gameData);
            } else if (next == TurnStep.DECLARE_ATTACKERS) {
                combatService.handleDeclareAttackersStep(gameData);
            } else if (next == TurnStep.DECLARE_BLOCKERS) {
                handleCombatResult(combatService.handleDeclareBlockersStep(gameData), gameData);
            } else if (next == TurnStep.COMBAT_DAMAGE) {
                handleCombatResult(combatService.resolveCombatDamage(gameData), gameData);
            } else if (next == TurnStep.END_OF_COMBAT) {
                processEndOfCombatActions(gameData);
                stepTriggerService.handleEndOfCombatTriggers(gameData);
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
                applyCleanupResets(gameData);
            }
        } else {
            advanceTurn(gameData);
        }
    }

    private boolean hasEndOfCombatActions(GameData gameData) {
        return gameData.hasDelayedAction(SacrificeAtEndOfCombat.class)
                || gameData.hasDelayedAction(DelayedPermanentAction.class,
                        action -> action.kind() == DelayedPermanentActionKind.EXILE_TOKEN_AT_END_OF_COMBAT
                                || action.kind() == DelayedPermanentActionKind.DESTROY_AT_END_OF_COMBAT
                                || action.kind() == DelayedPermanentActionKind.RETURN_TO_HAND_AT_END_OF_COMBAT
                                || action.kind() == DelayedPermanentActionKind.PUT_ON_TOP_OF_LIBRARY_AT_END_OF_COMBAT)
                || gameData.hasDelayedAction(DestroyEquipmentAtEndOfCombat.class)
                || gameData.hasDelayedAction(PutMinusOneCounterAtEndOfCombat.class)
                || gameData.hasDelayedAction(PutCounterOnPermanentAtEndOfCombat.class)
                || gameData.hasDelayedAction(RemoveCounterFromSourceAtEndOfCombat.class)
                || gameData.hasDelayedAction(GainControlOfPermanentAtEndOfCombat.class)
                || gameData.hasDelayedAction(ExileAndReturnTransformedAtEndOfCombat.class)
                || gameData.hasDelayedAction(DealDamageToPermanentAtEndOfCombat.class)
                || gameData.hasDelayedAction(DestroyCombatOpponentsAtEndOfCombat.class)
                || gameData.hasDelayedAction(DestroyCombatOpponentAtEndOfCombatThenPutCounterOnSource.class)
                || gameData.hasDelayedAction(TapAndSkipUntapAtEndOfCombat.class)
                || gameData.hasDelayedAction(TapCombatOpponentsAtEndOfCombat.class)
                || gameData.hasDelayedAction(PhaseOutAtEndOfCombat.class);
    }

    private void processEndOfCombatActions(GameData gameData) {
        combatService.processEndOfCombatSacrifices(gameData);
        combatService.processEndOfCombatTaps(gameData);
        combatService.processEndOfCombatCombatOpponentTaps(gameData);
        combatService.processEndOfCombatExiles(gameData);
        combatService.processEndOfCombatEquipmentDestruction(gameData);
        combatService.processEndOfCombatDestructions(gameData);
        combatService.processEndOfCombatCombatOpponentDestructions(gameData);
        combatService.processEndOfCombatSourceCounters(gameData);
        combatService.processEndOfCombatOpponentCounters(gameData);
        combatService.processEndOfCombatCounterRemovals(gameData);
        combatService.processEndOfCombatDamage(gameData);
        combatService.processEndOfCombatControlGains(gameData);
        combatService.processEndOfCombatExileAndReturnTransformed(gameData);
        combatService.processEndOfCombatPhaseOuts(gameData);
        combatService.processEndOfCombatReturnsToHand(gameData);
        combatService.processEndOfCombatLibraryTucks(gameData);
    }

    private void processAdditionalCombatBeginningEffects(GameData gameData) {
        DelayedAdditionalCombatBeginningEffect pending =
                gameData.drainFirstDelayedAction(DelayedAdditionalCombatBeginningEffect.class);
        if (pending != null) {
            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    pending.sourceCard(),
                    pending.controllerId(),
                    pending.sourceCard().getName() + "'s additional combat trigger",
                    new ArrayList<>(List.of(pending.effect())),
                    null,
                    pending.sourcePermanentId()
            ));
            gameLogService.append(gameData,
                    GameLog.cardThen(pending.sourceCard(), "'s additional combat trigger triggers."));
        }
    }

    private void processTargetCreatureMustAttackNextCombat(GameData gameData) {
        List<TargetCreatureMustAttackNextCombat> pendingActions =
                gameData.drainDelayedActions(TargetCreatureMustAttackNextCombat.class);
        for (TargetCreatureMustAttackNextCombat pending : pendingActions) {
            Permanent target = findPermanentOnAnyBattlefield(gameData, pending.permanentId());
            if (target == null) {
                continue;
            }
            UUID controllerId = gameData.findControllerOf(target);
            if (!gameData.activePlayerId.equals(controllerId)) {
                gameData.queueDelayedAction(pending);
                continue;
            }
            target.setMustAttackThisCombat(true);
            gameLogService.append(gameData,
                    GameLog.cardThen(target.getCard(), " must attack this combat if able."));
            log.info("Game {} - {} must attack this combat if able", gameData.id, target.getCard().getName());
        }
    }

    private TurnStep skipChosenPhases(GameData gameData, TurnStep next) {
        Set<SkipStepOrPhaseKind> skipped = gameData.skippedStepOrPhasesThisTurn
                .getOrDefault(gameData.activePlayerId, Set.of());
        while (next != null) {
            if (next == TurnStep.PRECOMBAT_MAIN && skipped.contains(SkipStepOrPhaseKind.MAIN_PHASE)) {
                logSkippedPhase(gameData, "main phase");
                next = TurnStep.BEGINNING_OF_COMBAT;
                continue;
            }
            if ((next == TurnStep.BEGINNING_OF_COMBAT || next == TurnStep.END_OF_COMBAT)
                    && skipped.contains(SkipStepOrPhaseKind.COMBAT_PHASE)) {
                logSkippedPhase(gameData, "combat phase");
                if (gameData.additionalCombatPhasesOnly > 0) {
                    gameData.additionalCombatPhasesOnly--;
                    if (gameData.aggressiveCombatPhasesOnly > 0) {
                        gameData.aggressiveCombatPhasesOnly--;
                    }
                    next = TurnStep.BEGINNING_OF_COMBAT;
                } else {
                    gameData.onlyAggressiveCreaturesCanAttackThisCombat = false;
                    next = TurnStep.POSTCOMBAT_MAIN;
                }
                continue;
            }
            if (next == TurnStep.POSTCOMBAT_MAIN && skipped.contains(SkipStepOrPhaseKind.MAIN_PHASE)) {
                logSkippedPhase(gameData, "main phase");
                if (gameData.additionalCombatMainPhasePairs > 0) {
                    gameData.additionalCombatMainPhasePairs--;
                    next = TurnStep.BEGINNING_OF_COMBAT;
                } else {
                    next = TurnStep.END_STEP;
                }
                continue;
            }
            break;
        }
        return next;
    }

    private boolean controlsAggressiveCreature(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        return battlefield != null && battlefield.stream()
                .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                .anyMatch(permanent -> gameQueryService.hasKeyword(gameData, permanent, Keyword.AGGRESSIVE));
    }

    private void logSkippedPhase(GameData gameData, String phaseName) {
        String message = gameData.playerIdToName.get(gameData.activePlayerId) + " skips their " + phaseName + ".";
        gameLogService.append(gameData, GameLog.text(message));
        log.info("Game {} - {}", gameData.id, message);
    }

    void advanceTurn(GameData gameData) {
        advanceTurn(gameData, true, true, null);
    }

    private void advanceTurn(GameData gameData, boolean snapshotEndingPlayer) {
        advanceTurn(gameData, snapshotEndingPlayer, true, null);
    }

    /** Completes Time Vault's turn-start replacement choice and resumes turn progression. */
    public void completeTimeVaultChoice(GameData gameData, UUID controllerId,
                                         UUID sourcePermanentId, boolean accepted) {
        if (accepted) {
            gameData.skipNextTurnCount.merge(controllerId, 1, Integer::sum);
            advanceTurn(gameData, false, false, null);
        } else {
            advanceTurn(gameData, false, true, sourcePermanentId);
        }
    }

    private void advanceTurn(GameData gameData, boolean snapshotEndingPlayer,
                             boolean offerTimeVaultChoice, UUID excludedTimeVaultId) {
        if (snapshotEndingPlayer) {
            gameData.snapshotPlayerActionsForLastTurn(gameData.activePlayerId);
        }
        // Clear any active mind control from the ending turn
        gameData.mindControlledPlayerId = null;
        gameData.mindControllerPlayerId = null;
        gameData.mindControlUntilEndOfCombat = false;

        UUID nextActive;
        Long extraTurnSequence = null;
        boolean currentTurnIsExtraTurn = false;
        boolean skipUntapStep = false;
        boolean powerUpAbilitiesDisabled = false;
        boolean damageCantBePrevented = false;
        UUID nextTurnPlayer = !gameData.extraTurns.isEmpty()
                ? gameData.extraTurns.peekFirst()
                : nextPlayerInTurnOrder(gameData);
        if (offerTimeVaultChoice) {
            Permanent timeVault = findTappedTimeVault(gameData, nextTurnPlayer, excludedTimeVaultId);
            if (timeVault != null) {
                queueTimeVaultChoice(gameData, nextTurnPlayer, timeVault);
                return;
            }
        }
        if (!gameData.extraTurns.isEmpty()) {
            nextActive = gameData.extraTurns.pollFirst();
            currentTurnIsExtraTurn = true;
            skipUntapStep = Boolean.TRUE.equals(gameData.extraTurnSkipsUntap.pollFirst());
            powerUpAbilitiesDisabled = Boolean.TRUE.equals(gameData.extraTurnPowerUpAbilitiesDisabled.pollFirst());
            damageCantBePrevented = Boolean.TRUE.equals(gameData.extraTurnDamageCantBePrevented.pollFirst());
            extraTurnSequence = gameData.extraTurnSequences.isEmpty()
                    ? null : gameData.extraTurnSequences.pollFirst();
            if (hasApplicableExtraTurnSkipReplacement(gameData, nextActive)) {
                Long skippedExtraTurnSequence = extraTurnSequence;
                if (skippedExtraTurnSequence != null) {
                    gameData.drainDelayedActions(LoseGameAtEndStep.class,
                            action -> skippedExtraTurnSequence.equals(action.extraTurnSequence()));
                }
                String skippedName = gameData.playerIdToName.get(nextActive);
                gameLogService.append(gameData, GameLog.text(skippedName + " skips their extra turn."));
                log.info("Game {} - {} skips their extra turn", gameData.id, skippedName);
                gameData.currentTurnIsExtraTurn = false;
                gameData.powerUpAbilitiesCantBeActivatedThisTurn = false;
                advanceTurn(gameData, false);
                return;
            }
        } else {
            nextActive = nextTurnPlayer;
        }

        // Chronatog: skip the turn entirely (CR 500.11 / 614.10). Pending Mindslaver waits (CR 723.1b).
        int queuedTurnSkips = gameData.skipNextTurnCount.getOrDefault(nextActive, 0);
        if (queuedTurnSkips > 0) {
            if (queuedTurnSkips == 1) {
                gameData.skipNextTurnCount.remove(nextActive);
            } else {
                gameData.skipNextTurnCount.put(nextActive, queuedTurnSkips - 1);
            }
            String skippedName = gameData.playerIdToName.get(nextActive);
            if (currentTurnIsExtraTurn && extraTurnSequence != null) {
                Long skippedExtraTurnSequence = extraTurnSequence;
                gameData.drainDelayedActions(LoseGameAtEndStep.class,
                        action -> skippedExtraTurnSequence.equals(action.extraTurnSequence()));
            }
            gameLogService.append(gameData, GameLog.text(skippedName + " skips their turn."));
            log.info("Game {} - {} skips their turn", gameData.id, skippedName);
            // Advance turn order past the skipped player so the next selection is correct.
            gameData.activePlayerId = nextActive;
            if (gameData.planechase != null) gameData.planechase.controllerId = nextActive;
            advanceTurn(gameData, false);
            return;
        }

        String nextActiveName = gameData.playerIdToName.get(nextActive);
        gameData.currentTurnIsExtraTurn = currentTurnIsExtraTurn;
        gameData.powerUpAbilitiesCantBeActivatedThisTurn = powerUpAbilitiesDisabled;
        gameData.damageCantBePreventedThisTurn = damageCantBePrevented;
        gameData.currentExtraTurnSequence = extraTurnSequence;

        // Yosei, the Morning Star: a queued "skips their next untap step" is consumed by the first
        // untap step this player would actually get (CR 614.10a).
        int queuedUntapSkips = gameData.skipNextUntapStepCount.getOrDefault(nextActive, 0);
        if (queuedUntapSkips > 0) {
            if (queuedUntapSkips == 1) {
                gameData.skipNextUntapStepCount.remove(nextActive);
            } else {
                gameData.skipNextUntapStepCount.put(nextActive, queuedUntapSkips - 1);
            }
            skipUntapStep = true;
            gameLogService.append(gameData, GameLog.text(nextActiveName + " skips their untap step."));
            log.info("Game {} - {} skips their untap step", gameData.id, nextActiveName);
        }

        gameData.activePlayerId = nextActive;
        gameData.turnStartTimestamp = gameData.timestampCounter + 1;
        if (gameData.planechase != null) gameData.planechase.controllerId = nextActive;
        if (gameData.skipCombatPhasesNextTurn.remove(nextActive)) {
            gameData.skippedStepOrPhasesThisTurn.computeIfAbsent(nextActive, id -> new HashSet<>())
                    .add(SkipStepOrPhaseKind.COMBAT_PHASE);
        }

        // Check for pending Taunt on the new active player: promote it to an active this-turn requirement
        gameData.tauntedThisTurn.clear();
        UUID taunter = gameData.tauntedNextTurn.remove(nextActive);
        if (taunter != null && gameData.playerIds.contains(taunter)) {
            gameData.tauntedThisTurn.put(nextActive, taunter);
            String taunterName = gameData.playerIdToName.get(taunter);
            String tauntLog = "Creatures " + nextActiveName + " controls must attack " + taunterName + " this turn if able.";
            gameLogService.append(gameData, GameLog.text(tauntLog));
            log.info("Game {} - {}'s creatures must attack {} this turn (Taunt)", gameData.id, nextActiveName, taunterName);
        }

        promoteSingleCreatureTaunts(gameData, nextActive);

        // Oracle en-Vec: promote the creatures the new active player chose last turn. They attack if
        // able, every other creature is barred, and each of them is destroyed at this turn's end step
        // if it didn't attack.
        gameData.chosenAttackersThisTurn.clear();
        gameData.attackableCreaturesThisTurn.clear();
        gameData.creaturesAbleToAttackAtDeclareAttackersThisTurn.clear();
        gameData.blockableCreaturesThisTurn.clear();
        Set<UUID> chosenAttackers = gameData.chosenAttackersNextTurn.remove(nextActive);
        if (chosenAttackers != null) {
            gameData.chosenAttackersThisTurn.put(nextActive, chosenAttackers);
            Set<UUID> markedForDestruction = gameData.chosenAttackersToDestroyNextTurn.remove(nextActive);
            for (UUID chosenId : markedForDestruction != null ? markedForDestruction : chosenAttackers) {
                gameData.queueDelayedAction(new DestroyPermanentIfDidNotAttackAtEndStep(chosenId));
            }
            gameLogService.append(gameData, GameLog.text("Only the " + chosenAttackers.size()
                    + " creature(s) " + nextActiveName + " chose can attack this turn, and they attack if able."));
            log.info("Game {} - {} is restricted to {} chosen attackers this turn (Oracle en-Vec)",
                    gameData.id, nextActiveName, chosenAttackers.size());
        }

        // Check for pending Mindslaver control on the new active player
        UUID pendingController = gameData.pendingTurnControl.remove(nextActive);
        boolean grantExtraTurnAfter = gameData.pendingTurnControlExtraTurn.remove(nextActive);
        if (pendingController != null && gameData.playerIds.contains(pendingController)) {
            gameData.mindControlledPlayerId = nextActive;
            gameData.mindControllerPlayerId = pendingController;
            gameData.mindControlUntilEndOfCombat = false;
            String controllerName = gameData.playerIdToName.get(pendingController);
            String controlLog = controllerName + " controls " + nextActiveName + " this turn (Mindslaver).";
            gameLogService.append(gameData, GameLog.text(controlLog));
            log.info("Game {} - {} controls {} this turn (Mindslaver)", gameData.id, controllerName, nextActiveName);
            // Emrakul: schedule the extra turn only once control actually activates (after that turn).
            if (grantExtraTurnAfter) {
                gameData.queueExtraTurnFirst(nextActive, false);
                String extraLog = nextActiveName + " takes an extra turn after this one.";
                gameLogService.append(gameData, GameLog.text(extraLog));
                log.info("Game {} - {} granted an extra turn after the controlled turn",
                        gameData.id, nextActiveName);
            }
        }
        gameData.turnNumber++;
        gameData.temporaryGlobalTriggeredAbilities.removeIf(watcher ->
                watcher.untilNextTurn()
                        && nextActive.equals(watcher.controllerId())
                        && gameData.turnNumber != watcher.registrationTurnNumber());
        gameData.cardPutIntoExileThisTurn = false;
        gameData.turnsTakenByPlayer.merge(nextActive, 1, Integer::sum);
        gameData.currentStep = TurnStep.first();
        gameData.additionalUpkeepsRemaining = 0;
        gameData.currentUpkeepIsAdditional = false;
        gameData.interaction.clearAwaitingInput();
        gameData.priorityPassedBy.clear();
        gameData.landsPlayedThisTurn.clear();
        gameData.playersWhoTappedLandForManaThisTurn.clear();
        gameData.additionalLandsThisTurn.clear();
        gameData.permanentsEnteredBattlefieldLastTurn.clear();
        gameData.permanentsEnteredBattlefieldThisTurn.forEach((playerId, entered) ->
                gameData.permanentsEnteredBattlefieldLastTurn.put(playerId, new ArrayList<>(entered)));
        gameData.permanentsEnteredBattlefieldThisTurn.clear();
        gameData.faceDownCreaturesEnteredBattlefieldThisTurn.clear();
        gameData.faceDownPermanentsEnteredBattlefieldThisTurn.clear();
        gameData.playersWhoTurnedPermanentsFaceUpThisTurn.clear();
        gameData.snapshotSpellCountsAndClear(gameData.spellsCastLastTurn);
        gameData.clearGreatestStackSourceCountThisTurn();
        gameData.crimeCandidatesThisTurn.clear();
        gameData.clearSpellsCastFromHandThisTurn();
        gameData.controllerNoncombatDamageBonusThisTurn.clear();
        gameData.playersWhoSearchedLibraryThisTurn.clear();
        gameData.playersWhoInvestigatedThisTurn.clear();
        gameData.playersWhoVenturedIntoDungeonThisTurn.clear();
        gameData.sacrificedPermanentSubtypeCountThisTurn.clear();
        gameData.sacrificedPermanentCountThisTurn.clear();
        gameData.permanentsSacrificedThisTurn.clear();
        gameData.playersWhoSurveilledThisTurn.clear();
        gameData.playersWhoFlippedCoinsThisTurn.clear();
        gameData.permanentTypesCastFromGraveyardThisTurn.clear();
        gameData.oncePerTurnGraveyardCastPermissionsUsedThisTurn.clear();
        gameData.playersDeclaredAttackersThisTurn.clear();
        gameData.playersWhoPutCountersOnCreaturesThisTurn.clear();
        gameData.permanentsWithCountersPutByPlayerThisTurn.clear();
        gameData.playersWhoPutPlusOnePlusOneCountersOnCreaturesThisTurn.clear();
        gameData.playersWhoRemovedOilCountersFromControlledPermanentsThisTurn.clear();
        gameData.permanentWithOilCounterPutIntoGraveyardThisTurn = false;
        gameData.artifactOrCreaturePutIntoGraveyardFromBattlefieldThisTurn = false;
        gameData.permanentPutIntoGraveyardFromBattlefieldThisTurn = false;
        gameData.playersWhoPutEnchantmentIntoGraveyardFromBattlefieldThisTurn.clear();
        gameData.permanentsPutIntoGraveyardFromBattlefieldThisTurn = 0;
        gameData.playersWhoControlledPermanentsThatReceivedPlusOneCountersThisTurn.clear();
        gameData.playersWhoSacrificedPermanentsThisTurn.clear();
        gameData.playersWhoSacrificedArtifactsThisTurn.clear();
        gameData.creaturesAttackedCountThisTurn.clear();
        gameData.creaturesAttackedCountBySubtypeThisTurn.clear();
        gameData.permanentsThatAttackedBattlesThisTurn.clear();
        gameData.playersSilencedThisTurn.clear();
        gameData.playersMaxSpellsThisTurn.clear();
        Set<CardType> nextTurnRestrictedTypes = gameData.playersCantCastSpellTypesNextTurn.remove(nextActive);
        if (nextTurnRestrictedTypes != null) {
            gameData.playersCantCastSpellTypesThisTurn.merge(nextActive, nextTurnRestrictedTypes,
                    (existing, pending) -> {
                        Set<CardType> merged = new HashSet<>(existing);
                        merged.addAll(pending);
                        return merged;
                    });
        }
        gameData.activatedAbilityUsesThisTurn.clear();
        gameData.playersWhoActivatedExhaustAbilityThisTurn.clear();
        gameData.playersWhoActivatedEquipAbilityThisTurn.clear();
        gameData.playersWhoActivatedLoyaltyAbilityThisTurn.clear();
        gameData.playersWhoActivatedSparkAbilityThisTurn.clear();
        gameData.permanentAbilityResolutionsThisTurn.clear();
        gameData.creatureCardsPutIntoGraveyardFromBattlefieldThisTurn.clear();
        gameData.cardsPutIntoGraveyardFromBattlefieldThisTurn.clear();
        gameData.artifactsPutIntoGraveyardFromBattlefieldThisTurn = 0;
        gameData.cardsPutIntoGraveyardFromAnywhereThisTurn.clear();
        gameData.creatureCardsPutIntoGraveyardFromAnywhereThisTurn.clear();
        gameData.playersWhoDescendedThisTurn.clear();
        gameData.descentsThisTurn.clear();
        gameData.playersWhoseNoncreaturePermanentsWereDestroyedByOpponentThisTurn.clear();
        gameData.playersWhoseCreatureSpellsWereCounteredByOpponentsThisTurn.clear();
        gameData.cardsDiscardedOrCycledThisTurn.clear();
        gameData.playersWhoReceivedPermanentFromBattlefieldToHandThisTurn.clear();
        gameData.cardsDiscardedByOpponentThisTurn.clear();
        gameData.playersWhosePermanentsLeftBattlefieldThisTurn.clear();
        gameData.creatureLeftBattlefieldCountThisTurn.clear();
        gameData.nonlandPermanentLeftBattlefieldThisTurn = false;
        gameData.creatureDeathCountThisTurn.clear();
        gameData.creatureNamesDiedThisTurn.clear();
        gameData.creaturesPutIntoOwnGraveyardThisTurnCount.clear();
        gameData.nontokenCreaturesPutIntoOwnGraveyardThisTurnCount.clear();
        gameData.nontokenCreatureDeathCountThisTurn.clear();
        gameData.creatureSubtypeDeathCountThisTurn.clear();
        gameData.cardsDrawnThisTurn.clear();
        gameData.cardsDrawnThisTurnIds.clear();
        gameData.cardsDiscardedThisTurn.clear();
        gameData.lifeGainedThisTurn.clear();
        gameData.lifeLostLastTurn.clear();
        gameData.lifeLostLastTurn.putAll(gameData.lifeLostThisTurn);
        gameData.lifeLostThisTurn.clear();
        gameData.playersDealtCombatDamageLastTurn.clear();
        if (gameData.playersDealtCombatDamageSinceTheirLastTurn.remove(nextActive)) {
            gameData.playersDealtCombatDamageLastTurn.add(nextActive);
        }
        gameData.combatDamageToPlayersThisTurn.clear();
        gameData.combatDamageDealtToPlayersThisTurn.clear();
        gameData.combatDamageSourcesThatDealtToCreaturesThisTurn.clear();
        gameData.noncombatDamageToPlayersThisTurn.clear();
        gameData.creatureDamageToPlayersThisTurn.clear();
        gameData.damageDealtThisTurnBySource.clear();
        gameData.damageDealtToPlayersBySourceThisTurn.clear();
        gameData.sorcerySpellDamageDealtThisTurn.clear();
        gameData.damageSourcesControlledByPlayerThisTurn.clear();
        gameData.playersAttackedThisTurn.clear();
        gameData.playersWhoAttackedPlayerOrPlaneswalkerThisTurn.clear();
        gameData.playersWhoAttackedPlayersThisTurn.clear();
        gameData.creaturesThatSaddledPermanentThisTurn.clear();
        gameData.creaturesThatCrewedPermanentThisTurn.clear();
        gameData.clearDelayedActions(DelayedCombatDamageLoot.class);
        gameData.clearDelayedActions(DelayedCombatDamageToken.class);
        gameData.clearDelayedActions(DelayedCombatDamageDraw.class);
        gameData.clearDelayedActions(DelayedCombatDamageLookAtHandAndDraw.class);
        gameData.clearDelayedActions(DelayedCombatDamageReflection.class);
        // Conduit of Storms: "next main phase this turn" — drop any that never fired.
        gameData.clearDelayedActions(AddManaAtNextMainPhase.class, AddManaAtNextMainPhase::thisTurnOnly);
        gameData.clearDelayedActions(DelayedBlockerBoost.class);
        gameData.clearDelayedActions(DelayedAttackerBoost.class);
        gameData.clearDelayedActions(DelayedAttackerKeywordGrant.class);
        gameData.clearDelayedActions(DelayedNontokenAttackTokenCreation.class);
        gameData.clearDelayedActions(DelayedAttackTokenCreation.class);
        gameData.clearDelayedActions(DelayedAttackDamage.class);
        gameData.clearDelayedActions(DelayedAttackUntap.class);
        gameData.clearDelayedActions(DelayedVehicleAttack.class);
        gameData.clearDelayedActions(DelayedControllerSpellCastTrigger.class);
        gameData.clearDelayedActions(DelayedUnblockedAttackerGainLife.class);
        gameData.clearDelayedActions(DelayedUnblockedAttackerPowerDamage.class);
        gameData.clearDelayedActions(DelayedUnblockedAttackerCubeCounter.class);
        gameData.clearDelayedActions(DelayedDestroyCreatureDamagedByWatchedCreature.class);
        gameData.clearDelayedActions(DelayedSacrificeSourceWhenTargetLeaves.class);
        gameData.clearDelayedActions(DelayedSacrificeTargetWhenSourceLeaves.class);
        gameData.clearDelayedActions(DelayedDestroyTargetWhenSourceLeaves.class);
        gameData.clearDelayedActions(DelayedAdditionalCombatBeginningEffect.class);
        gameData.clearDelayedActions(DelayedBeginningOfCombatTrigger.class);
        gameData.combatDamageSourceSubtypesThisTurn.clear();
        gameData.combatDamageSourcesWithChangelingThisTurn.clear();
        gameData.combatDamageSourcesWithLegendaryThisTurn.clear();
        gameData.combatDamageToPlayerControllerSubtypesThisTurn.clear();
        gameData.controllersDealtCombatDamageWithChangelingThisTurn.clear();
        gameData.combatBlockOpponentSubtypesThisTurn.clear();
        gameData.combatBlockOpponentColorsThisTurn.clear();
        gameData.creaturesInCombatWithChangelingThisTurn.clear();
        gameData.combatBlockOpponentIdsThisTurn.clear();
        gameData.combatBlockOpponentControllerIdsThisTurn.clear();
        gameData.combatOpponentIdsBlockedByThisTurn.clear();
        gameData.creaturesBlockedThisTurn.clear();
        gameData.playersDealtDamageThisTurn.clear();
        gameData.damageDealtToPlayersThisTurn.clear();
        gameData.artifactDamageDealtToPlayersThisTurn.clear();
        gameData.noncombatDamageDealtToPlayersThisTurn.clear();
        gameData.redSourceNoncombatDamageThisTurn.clear();
        gameData.lastRedSpellDamagerThisTurn.clear();
        gameData.untappedLandsAtTurnStart.clear();
        gameData.handSizeAtTurnStart.clear();
        List<Card> handAtTurnStart = gameData.playerHands.get(nextActive);
        gameData.handSizeAtTurnStart.put(nextActive, handAtTurnStart == null ? 0 : handAtTurnStart.size());
        gameData.permanentsDealtDamageThisTurn.clear();
        gameData.permanentsDealtNoncombatDamageThisTurn.clear();
        gameData.permanentsDealtExcessDamageThisTurn.clear();
        gameData.damageDealtToPermanentsThisTurn.clear();
        gameData.damageDealtToPermanentsBySourceThisTurn.clear();
        gameData.damageSourceNamesThisTurn.clear();
        gameData.controllersOfPermanentsDealtExcessDamageThisTurn.clear();
        gameData.qualifyingDamageControllersByPermanentThisTurn.clear();
        gameData.freeCastPermanentUsedThisTurn.clear();
        gameData.oncePerTurnExileCastPermissionsUsedThisTurn.clear();
        gameData.oncePerTurnLibraryCastPermissionsUsedThisTurn.clear();
        gameData.oncePerTurnTriggersFiredThisTurn.clear();
        gameData.keyedOncePerTurnTriggersFiredThisTurn.clear();
        gameData.oncePerCreatureTriggersFiredThisTurn.clear();
        gameData.creatureTapCountsThisTurn.clear();
        gameData.permanentsThatAddedManaWithAbilityThisTurn.clear();
        gameData.firstResolutionTriggerKeysThisTurn.clear();
        gameData.permanentsThatReceivedPlusOnePlusOneCountersThisTurn.clear();
        gameData.plusOnePlusOneCountersPutOnControlledCreaturesThisTurn.clear();
        gameData.onceEachTurnAttackTriggersFiredThisTurn.clear();
        gameData.bendingTypesCompletedThisTurn.clear();
        gameData.tokenCreationReplacementUsedThisTurn.clear();
        gameData.creatureCardsDamagedThisTurnBySourcePermanent.clear();
        gameData.sourcesThatDealtDamageToCreaturesThisTurn.clear();
        gameData.sourcesWhoseDamagedCreaturesDiedThisTurn.clear();
        gameData.creatureCardsDamagedBySourceThatDiedThisTurn.clear();
        gameData.creatureGivingControllerPoisonOnDeathThisTurn.clear();
        gameData.creaturesReturnedToBattlefieldOnDeathThisTurn.clear();
        gameData.playersAffectedByMeliraPoisonReplacementThisTurn.clear();
        gameData.creatureTriggeringEffectOnDeathThisTurn.clear();
        gameData.additionalCombatMainPhasePairs = 0;
        gameData.additionalCombatMainPhasePairsReturnStep = null;
        gameData.additionalCombatPhasesOnly = 0;
        gameData.aggressiveCombatPhasesOnly = 0;
        gameData.additionalCombatReturnActivePlayerId = null;
        gameData.onlyLandCreaturesCanAttackThisCombat = false;
        gameData.onlyAggressiveCreaturesCanAttackThisCombat = false;
        gameData.additionalCombatPhasesAfterMain = 0;
        gameData.additionalCombatPhasesAfterMainReturnStep = null;
        gameData.additionalUpkeepStepsAfterCombat = 0;
        gameData.additionalUpkeepReturnStep = null;
        gameData.additionalBeginningPhasesAfterCombat = 0;
        gameData.additionalBeginningPhaseReturnStep = null;
        gameData.additionalBeginningPhaseUntapInProgress = false;
        gameData.cardsGrantedFlashbackWithoutPayingManaCostUntilEndOfTurn.clear();
        gameData.combatPhasesThisTurn = 0;
        gameData.endStepsThisTurn = 0;
        gameData.additionalEndStepsPending = 0;
        gameData.cleanupDiscardPending = false;
        gameData.paidSearchTaxPermanentIds.clear();
        gameData.otherCreaturesCantAttackExemptCreatureIds.clear();
        gameData.creaturesCantAttackThisTurn = false;
        if (gameData.peaceTalksTurnsRemaining > 0) {
            gameData.peaceTalksTurnsRemaining--;
        }

        turnCleanupService.drainManaPools(gameData);

        gameData.forEachPermanent((playerId, p) -> {
            p.setAttackedThisTurn(false);
            p.setAttacksThisTurn(0);
            p.setBlockedThisTurn(false);
            p.setBecomeTargetCounterUsedThisTurn(false);
            p.setBackupAbilityCopyUsedThisTurn(false);
            p.getChosenModeLabelsThisTurn().clear();
        });
        gameData.phasedOutPermanents.values().forEach(
                battlefield -> battlefield.forEach(p -> p.getChosenModeLabelsThisTurn().clear()));

        // Clear "until your next turn" activated abilities for the active player's permanents
        List<Permanent> activePlayerBf = gameData.playerBattlefields.get(nextActive);
        if (activePlayerBf != null) {
            activePlayerBf.forEach(Permanent::clearUntilNextTurnEffects);
            // Halls of Mist: "attacked during their controller's last turn" is scoped to the
            // controller's own turns, so the record shifts only when that player's turn begins.
            activePlayerBf.forEach(Permanent::rollOverAttackRecord);
            // Promote/expire Wall of Dust "can't attack next turn" restrictions on the active player's
            // creatures (scoped to their controller's turn so it never arms on an opponent's turn).
            activePlayerBf.forEach(Permanent::promoteCantAttackNextTurn);
        }
        gameData.forEachPermanent((controllerId, permanent) ->
                permanent.clearLosesAllAbilitiesUntilNextTurnController(nextActive));
        gameData.additionalEnterCountersUntilNextTurn.remove(nextActive);
        gameData.cardTypeFlashGrantsUntilNextTurn.remove(nextActive);
        // Gideon of the Trials +1: "until your next turn" damage-dealing prevention ends now for the
        // player whose turn is beginning (its entries are keyed by that controlling player).
        gameData.permanentsPreventedFromDealingDamageUntilNextTurn.values().removeIf(nextActive::equals);
        gameData.permanentsProtectedFromDamageUntilNextTurn.values().removeIf(nextActive::equals);
        gameData.creatureControllerDamageRedirectShields.removeIf(
                shield -> nextActive.equals(shield.protectedPlayerId()));
        // Comply: "until your next turn, your opponents can't cast spells with the chosen name".
        gameData.opponentsCantCastNamedSpellsUntilControllerNextTurn.remove(nextActive);
        gameData.opponentsCantCastSpellsWithManaValueUntilControllerNextTurn.remove(nextActive);
        gameData.playersCantCastNamedSpellsUntilControllerNextTurn.remove(nextActive);
        gameData.spellsAndLandsWithChosenNameCantBePlayedUntilControllerNextTurn.remove(nextActive);
        gameData.playersCantCastNoncreatureSpellsUntilControllerNextTurn.remove(nextActive);
        gameData.cardsRevealedInHandUntilOwnerNextTurn.values().removeIf(nextActive::equals);
        gameData.cardsCantBePlayedInHandUntilOwnerNextTurn.values().removeIf(nextActive::equals);
        gameData.playersCantCastSpellsFromOutsideHandUntilControllerNextTurn.remove(nextActive);
        gameData.playersCantPlayCardsFromHandUntilControllerNextTurn.remove(nextActive);
        gameData.cardTypeFlashGrantsUntilNextTurn.remove(nextActive);
        gameData.playersWithNoMaximumHandSizeUntilNextTurn.remove(nextActive);
        gameData.playersWithAllPlayerDamagePreventedUntilNextTurn.remove(nextActive);
        gameData.playersWithProtectionFromEverythingUntilNextTurn.remove(nextActive);
        gameData.playersWithLifeTotalCantChangeUntilNextTurn.remove(nextActive);
        gameData.playerKeywordsUntilNextTurn.remove(nextActive);
        // Jace, Architect of Thought +1: the delayed "whenever a creature an opponent controls
        // attacks" trigger lasts until its controller's next turn, so it expires here rather than at
        // turn cleanup like the other delayed families.
        gameData.clearDelayedActions(DelayedOpponentAttackerBoost.class,
                boost -> boost.controllerId().equals(nextActive));
        gameData.clearDelayedActions(DelayedDestroyCreatureDealingCombatDamageToPlaneswalker.class,
                trigger -> trigger.controllerId().equals(nextActive));
        gameData.clearDelayedActions(DelayedDamageDoubling.class,
                doubling -> doubling.controllerId().equals(nextActive));
        // Tamiyo, Field Researcher +1: the "whenever either of those creatures deals combat damage"
        // watch lasts until its controller's next turn, so it expires here too.
        gameData.clearDelayedActions(DelayedWatchedCreaturesCombatDamage.class,
                watch -> !watch.untilEndOfTurn() && watch.controllerId().equals(nextActive));
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

        gameData.turnUntapStepSkipped = skipUntapStep;
        gameData.captureTurnStartSnapshot();

        // Savor the Moment (extra-turn flag) or Sands of Time (global static): skip the entire
        // untap step — no phasing, no Storage Matrix / Static Orb choice — but summoning sickness
        // still clears.
        if (skipUntapStep || untapStepService.playersSkipUntapStepApplies(gameData)) {
            untapStepService.untapPermanents(gameData, nextActive, null, true);
        } else {
            // Storage Matrix: pause the untap step so the active player chooses artifact/creature/land
            // before untapping. The choice handler resumes via resumeStorageMatrixUntap.
            if (untapStepService.storageMatrixRestrictionApplies(gameData, nextActive)) {
                playerInputService.beginStorageMatrixUntapChoice(gameData, nextActive);
                invalidateForAllPlayers(gameData);
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
                invalidateForAllPlayers(gameData);
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

    private boolean hasApplicableExtraTurnSkipReplacement(GameData gameData, UUID extraTurnPlayerId) {
        for (var battlefieldEntry : gameData.playerBattlefields.entrySet()) {
            UUID sourceControllerId = battlefieldEntry.getKey();
            for (Permanent permanent : battlefieldEntry.getValue()) {
                boolean applies = permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                        .filter(ExtraTurnSkipReplacementEffect.class::isInstance)
                        .map(ExtraTurnSkipReplacementEffect.class::cast)
                        .anyMatch(effect -> effect.appliesToExtraTurn(sourceControllerId, extraTurnPlayerId));
                if (applies) return true;
            }
        }
        return false;
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

        if (gameData.additionalBeginningPhaseUntapInProgress) {
            completeAdditionalBeginningPhaseUntap(gameData);
        } else {
            completeTurnAdvance(gameData);
        }
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

        if (gameData.additionalBeginningPhaseUntapInProgress) {
            completeAdditionalBeginningPhaseUntap(gameData);
        } else {
            completeTurnAdvance(gameData);
        }
    }

    private void beginAdditionalBeginningPhase(GameData gameData) {
        gameData.additionalBeginningPhaseUntapInProgress = true;
        UUID activePlayerId = gameData.activePlayerId;
        String activePlayerName = gameData.playerIdToName.get(activePlayerId);
        boolean skipUntapStep = false;
        int queuedUntapSkips = gameData.skipNextUntapStepCount.getOrDefault(activePlayerId, 0);
        if (queuedUntapSkips > 0) {
            if (queuedUntapSkips == 1) {
                gameData.skipNextUntapStepCount.remove(activePlayerId);
            } else {
                gameData.skipNextUntapStepCount.put(activePlayerId, queuedUntapSkips - 1);
            }
            skipUntapStep = true;
            gameLogService.append(gameData, GameLog.text(activePlayerName + " skips their untap step."));
        }

        if (skipUntapStep || untapStepService.playersSkipUntapStepApplies(gameData)) {
            untapStepService.untapPermanents(gameData, activePlayerId, null, true);
        } else if (untapStepService.storageMatrixRestrictionApplies(gameData, activePlayerId)) {
            playerInputService.beginStorageMatrixUntapChoice(gameData, activePlayerId);
            invalidateForAllPlayers(gameData);
            return;
        } else {
            java.util.Optional<com.github.laxika.magicalvibes.model.effect.StaticOrbEffect> untapRestriction =
                    untapStepService.bindingUntapRestriction(gameData, activePlayerId);
            if (untapRestriction.isPresent()) {
                com.github.laxika.magicalvibes.model.effect.StaticOrbEffect effect = untapRestriction.get();
                playerInputService.beginStaticOrbUntapChoice(gameData, activePlayerId,
                        untapStepService.staticOrbUntapCandidates(gameData, activePlayerId, effect),
                        effect.maxUntap(), effect.filter());
                invalidateForAllPlayers(gameData);
                return;
            }
            untapStepService.untapPermanents(gameData, activePlayerId);
        }

        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
            return;
        }
        completeAdditionalBeginningPhaseUntap(gameData);
    }

    /** Finishes the untap step of an inserted beginning phase and advances to its upkeep. */
    public void completeAdditionalBeginningPhaseUntap(GameData gameData) {
        untapStepService.finishUntapStep(gameData, gameData.activePlayerId);
        advanceStep(gameData);
        gameData.additionalBeginningPhaseUntapInProgress = false;
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
        untapStepService.finishUntapStep(gameData, gameData.activePlayerId);
        String activeName = gameData.playerIdToName.get(gameData.activePlayerId);
        String logEntry = "Turn " + gameData.turnNumber + " begins. " + activeName + "'s turn.";
        gameLogService.append(gameData, GameLog.text(logEntry));
        log.info("Game {} - Turn {} begins. Active player: {}", gameData.id, gameData.turnNumber, activeName);
        invalidateForAllPlayers(gameData);
    }

    /** Restarts the current turn from its untap step after a turn-start snapshot was restored. */
    public void restartTurnFromBeginning(GameData gameData) {
        gameData.currentStep = TurnStep.UNTAP;
        gameData.priorityPassedBy.clear();
        gameData.interaction.clearAwaitingInput();
        gameData.captureTurnStartSnapshot();

        if (gameData.turnUntapStepSkipped || untapStepService.playersSkipUntapStepApplies(gameData)) {
            untapStepService.untapPermanents(gameData, gameData.activePlayerId, null, true);
        } else {
            untapStepService.untapPermanents(gameData, gameData.activePlayerId);
        }

        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
            return;
        }

        completeTurnAdvance(gameData);
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
        if (gameData.waitingForSubgame) return;
        // Process pending may abilities before auto-passing (e.g. attack-triggered "you may" effects)
        // Only when the stack is empty — otherwise stack items (e.g. Time Stop) must resolve first
        if (gameData.stack.isEmpty() && !gameData.pendingMayAbilities.isEmpty()
                && !gameData.interaction.isAwaitingInput()) {
            playerInputService.processNextMayAbility(gameData);
            return;
        }
        autoPassService.resolveAutoPass(gameData, this::advanceStep);
    }

    public void completePendingExileReturnAttackTarget(GameData gameData, UUID attackTargetId,
                                                        PermanentChoiceContext.ExileReturnAttackTarget context) {
        stepTriggerService.resolvePendingExileReturnAttackTarget(gameData, attackTargetId, context);
        if (!gameData.interaction.isAwaitingInput()) {
            combatService.handleDeclareAttackersStep(gameData);
        }
    }

    public void applyCleanupResets(GameData gameData) {
        stepTriggerService.handleCleanupTriggers(gameData);
        turnCleanupService.applyCleanupResets(gameData);
    }

    public void processNextUpkeepAnyTargetTrigger(GameData gameData) {
        stepTriggerService.processNextUpkeepAnyTargetTrigger(gameData);
    }

    public void processNextUpkeepModalTrigger(GameData gameData) {
        stepTriggerService.processNextUpkeepModalTrigger(gameData);
    }

    public void queueChosenModeUpkeepTrigger(GameData gameData, Card sourceCard, UUID controllerId,
            UUID permanentId, ChooseOneEffect.ChooseOneOption chosen) {
        stepTriggerService.queueChosenModeUpkeepTrigger(gameData, sourceCard, controllerId, permanentId, chosen);
    }

    public void processNextUpkeepPermanentTarget(GameData gameData) {
        stepTriggerService.processNextUpkeepPermanentTarget(gameData);
    }

    public void processNextPhasesInTriggerTarget(GameData gameData) {
        stepTriggerService.processNextPhasesInTriggerTarget(gameData);
    }

    public void processNextUpkeepPlayerTarget(GameData gameData) {
        stepTriggerService.processNextUpkeepPlayerTarget(gameData);
    }

    public void processNextMainPhasePlayerTarget(GameData gameData) {
        stepTriggerService.processNextMainPhasePlayerTarget(gameData);
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

    private UUID nextPlayerInTurnOrder(GameData gameData) {
        List<UUID> ids = new ArrayList<>(gameData.orderedPlayerIds);
        UUID currentActive = gameData.activePlayerId;
        return ids.get(0).equals(currentActive) ? ids.get(1) : ids.get(0);
    }

    private Permanent findTappedTimeVault(GameData gameData, UUID controllerId, UUID excludedPermanentId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return null;
        }
        return battlefield.stream()
                .filter(permanent -> excludedPermanentId == null
                        || !excludedPermanentId.equals(permanent.getId()))
                .filter(Permanent::isTapped)
                .filter(permanent -> !permanent.isFaceDown())
                .filter(permanent -> !gameQueryService.hasLostAllAbilities(gameData, permanent))
                .filter(permanent -> gameQueryService.hasActiveStaticEffect(
                        gameData, permanent, TimeVaultReplacementEffect.class))
                .findFirst()
                .orElse(null);
    }

    private void queueTimeVaultChoice(GameData gameData, UUID controllerId, Permanent timeVault) {
        gameData.pendingMayAbilities.add(new PendingMayAbility(
                timeVault.getCard(),
                controllerId,
                List.of(new TimeVaultReplacementEffect()),
                "Skip your turn and untap Time Vault?",
                null,
                null,
                timeVault.getId()));
        playerInputService.processNextMayAbility(gameData);
    }

    /**
     * Promotes the delayed single-creature taunts recorded for creatures the new active player
     * controls (Gideon, Battle-Forged's +2) onto the creatures themselves, so declare-attackers sees
     * the same transient pair Alluring Siren sets. The entry is consumed even when the permanent it
     * named is gone: a requirement that can no longer be obeyed simply lapses.
     */
    private void promoteSingleCreatureTaunts(GameData gameData, UUID nextActive) {
        if (gameData.creatureMustAttackPermanentNextTurn.isEmpty()) {
            return;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(nextActive);
        if (battlefield == null) {
            return;
        }
        for (Permanent creature : battlefield) {
            UUID attackTargetId = gameData.creatureMustAttackPermanentNextTurn.remove(creature.getId());
            if (attackTargetId == null) {
                continue;
            }
            Permanent attackTarget = findPermanentOnAnyBattlefield(gameData, attackTargetId);
            if (attackTarget == null) {
                continue;
            }
            creature.setMustAttackThisTurn(true);
            creature.setMustAttackTargetId(attackTargetId);
            gameLogService.append(gameData, GameLog.builder()
                    .card(creature.getCard())
                    .text(" must attack " + attackTarget.getCard().getName() + " this turn if able.")
                    .build());
            log.info("Game {} - {} must attack {} this turn", gameData.id,
                    creature.getCard().getName(), attackTarget.getCard().getName());
        }
    }

    private Permanent findPermanentOnAnyBattlefield(GameData gameData, UUID permanentId) {
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            for (Permanent permanent : battlefield) {
                if (permanent.getId().equals(permanentId)) {
                    return permanent;
                }
            }
        }
        return null;
    }

    private void invalidateForAllPlayers(GameData gameData) {
        mutationCoordinator.emit(gameData,
                new GameEventFact.StateInvalidated(
                        GameEventFact.StateSection.TURN_AND_PRIORITY),
                GameEventAudience.allPlayers());
    }
}
