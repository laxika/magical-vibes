package com.github.laxika.magicalvibes.service.turn;

import com.github.laxika.magicalvibes.model.effect.ControlPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.SkipKind;
import com.github.laxika.magicalvibes.model.effect.SkipNextEffect;
import com.github.laxika.magicalvibes.model.effect.DamageCantBePreventedThisTurnEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.OnlyMatchingCreaturesCanAttackThisCombatEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.rules.TurnEvent;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.OnlySourceCreatureCanAttackThisCombatEffect;
import com.github.laxika.magicalvibes.model.effect.PowerUpAbilitiesCantBeActivatedEffect;
import com.github.laxika.magicalvibes.model.effect.ExtraTurnSkipReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.RunedTerrorEffect;
import com.github.laxika.magicalvibes.model.effect.SkipStepOrPhaseKind;
import com.github.laxika.magicalvibes.model.effect.TimeVaultReplacementEffect;
import com.github.laxika.magicalvibes.model.event.GameEventAudience;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.combat.CombatResult;
import com.github.laxika.magicalvibes.service.combat.CombatService;
import com.github.laxika.magicalvibes.service.event.GameMutationCoordinator;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TurnProgressionService {
    @org.springframework.beans.factory.annotation.Autowired
    @org.springframework.context.annotation.Lazy
    private com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService predicateEvaluationService;

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
        boolean runedTerrorActive = hasRunedTerror(gameData);
        if (!runedTerrorActive) {
            gameData.runedTerrorPhaseSequenceActive = false;
            gameData.runedTerrorPhaseSequenceFirstPlayerId = null;
        }

        // The mana pool drains at step boundaries, so nothing recorded before this point can
        // still be reverted by the cancel-casting UI.
        gameData.revertableManaActivations.clear();

        if (gameData.currentStep == TurnStep.END_OF_COMBAT) {
            combatService.clearCombatState(gameData);
            gameData.playerManaPools.values().forEach(manaPool -> manaPool.clearCombatMana());
        }

        gameData.resetPriorityToActivePlayer();
        gameData.interaction.clearAwaitingInput();

        if (gameData.currentStep == TurnStep.COMBAT_DAMAGE
                && ((gameData.pendingCombatDamageState != null
                && gameData.pendingCombatDamageState.awaitingOptionalDamageChoices)
                || ((gameData.combatDamageFirstestStrikeStepComplete
                || gameData.combatDamageFirstStrikeStepComplete)
                && !gameData.combatDamagePhase1Complete))) {
            handleCombatResult(combatService.resolveCombatDamage(gameData), gameData);
            return;
        }

        // The step or phase ends: the effects that last until its end end (CR 500.5).
        if (gameData.currentStep != null) {
            gameData.rulesEvents.occur(new TurnEvent.StepEnded(gameData.currentStep, gameData.activePlayerId));
        }

        if (runedTerrorActive && advanceRunedTerrorPhase(gameData)) {
            return;
        }

        TurnStep next = gameData.currentStep.next();
        boolean nextUpkeepIsAdditional = false;
        boolean additionalBeginningPhaseUntap = false;
        boolean additionalBeginningPhaseUpkeep = gameData.currentStep == TurnStep.UNTAP
                && gameData.additionalBeginningPhaseUntapInProgress;

        if (gameData.currentStep == TurnStep.UPKEEP && gameData.additionalUpkeepsRemaining > 0) {
            next = TurnStep.UPKEEP;
            gameData.additionalUpkeepsRemaining--;
            nextUpkeepIsAdditional = true;
        }

        boolean additionalCombatPhase = false;
        boolean aggressiveCombat = false;

        if (next == TurnStep.UPKEEP && stepTriggerService.playersSkipUpkeepStepApplies(gameData)) {
            if (nextUpkeepIsAdditional) {
                while (gameData.additionalUpkeepsRemaining > 0) {
                    gameData.additionalUpkeepsRemaining--;
                }
                next = TurnStep.DRAW;
            } else if (gameData.currentStep == TurnStep.UNTAP) {
                next = TurnStep.DRAW;
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

        if (gameData.currentStep == TurnStep.POSTCOMBAT_MAIN) {
            gameData.additionalCombatMainPhasePairsAfterSecondMain = 0;
        }
        if ((gameData.currentStep == TurnStep.PRECOMBAT_MAIN
                || gameData.currentStep == TurnStep.POSTCOMBAT_MAIN)
                && gameData.additionalCombatMainPhasePairs > gameData.additionalCombatMainPhasePairsAfterSecondMain) {
            next = TurnStep.BEGINNING_OF_COMBAT;
            gameData.additionalCombatMainPhasePairs--;
            additionalCombatPhase = true;
        } else if (gameData.currentStep == TurnStep.POSTCOMBAT_MAIN
                && gameData.additionalCombatMainPhasePairsReturnStep != null) {
            next = gameData.additionalCombatMainPhasePairsReturnStep;
            gameData.additionalCombatMainPhasePairsReturnStep = null;
        }

        boolean beginningPhaseBeforeQueuedCombat = gameData.currentStep == TurnStep.POSTCOMBAT_MAIN
                && additionalCombatPhase
                && next == TurnStep.BEGINNING_OF_COMBAT
                && gameData.additionalBeginningPhasesBeforeQueuedCombat > 0
                && gameData.additionalBeginningPhasesAfterPostcombatMain > 0;
        if (gameData.currentStep == TurnStep.POSTCOMBAT_MAIN
                && (next == TurnStep.END_STEP || beginningPhaseBeforeQueuedCombat)
                && gameData.additionalBeginningPhasesAfterPostcombatMain > 0) {
            // A phase created later happens before phases created earlier, so a beginning phase
            // created during this main phase precedes an already queued extra combat phase.
            gameData.additionalBeginningPhaseReturnStep = next;
            next = TurnStep.UNTAP;
            gameData.additionalBeginningPhasesAfterPostcombatMain--;
            if (beginningPhaseBeforeQueuedCombat) {
                gameData.additionalBeginningPhasesBeforeQueuedCombat--;
            }
            additionalBeginningPhaseUntap = true;
        } else if (gameData.currentStep == TurnStep.DRAW
                && gameData.additionalBeginningPhaseReturnStep != null) {
            boolean returningToQueuedCombat =
                    gameData.additionalBeginningPhaseReturnStep == TurnStep.BEGINNING_OF_COMBAT;
            if (gameData.additionalBeginningPhasesAfterPostcombatMain > 0
                    && (!returningToQueuedCombat || gameData.additionalBeginningPhasesBeforeQueuedCombat > 0)) {
                next = TurnStep.UNTAP;
                gameData.additionalBeginningPhasesAfterPostcombatMain--;
                if (returningToQueuedCombat) {
                    gameData.additionalBeginningPhasesBeforeQueuedCombat--;
                }
                additionalBeginningPhaseUntap = true;
            } else {
                next = gameData.additionalBeginningPhaseReturnStep;
                gameData.additionalBeginningPhaseReturnStep = null;
                additionalCombatPhase = next == TurnStep.BEGINNING_OF_COMBAT;
            }
        }

        if (next == TurnStep.DRAW
                && (stepTriggerService.skipsDrawStepByStaticAbility(gameData, gameData.activePlayerId)
                || skips(gameData, SkipStepOrPhaseKind.DRAW_STEP))) {
            if (gameData.additionalBeginningPhaseReturnStep != null) {
                if (gameData.additionalBeginningPhasesAfterPostcombatMain > 0) {
                    next = TurnStep.UNTAP;
                    gameData.additionalBeginningPhasesAfterPostcombatMain--;
                    additionalBeginningPhaseUntap = true;
                } else {
                    next = gameData.additionalBeginningPhaseReturnStep;
                    gameData.additionalBeginningPhaseReturnStep = null;
                }
            } else {
                next = TurnStep.PRECOMBAT_MAIN;
            }
            logSkippedPhase(gameData, "draw");
        }

        if ((gameData.currentStep == TurnStep.PRECOMBAT_MAIN
                || gameData.currentStep == TurnStep.POSTCOMBAT_MAIN)
                && gameData.additionalCombatPhasesAfterMain > 0) {
            next = TurnStep.BEGINNING_OF_COMBAT;
            gameData.additionalCombatPhasesAfterMain--;
            additionalCombatPhase = true;
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
                aggressiveCombat = true;
            }
            additionalCombatPhase = true;
        }

        if (gameData.currentStep == TurnStep.END_OF_COMBAT
                && gameData.additionalCombatPhasesOnly == 0
                && gameData.additionalCombatPhasesAfterMain > 0) {
            next = TurnStep.BEGINNING_OF_COMBAT;
            gameData.additionalCombatPhasesAfterMain--;
            additionalCombatPhase = true;
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

        boolean extraCombatFirst = gameData.currentStep == TurnStep.END_OF_COMBAT
                && additionalCombatPhase && gameData.additionalCombatCreatedAfterUpkeepSteps;
        if (extraCombatFirst) {
            gameData.additionalCombatCreatedAfterUpkeepSteps = false;
        } else if (gameData.currentStep == TurnStep.END_OF_COMBAT
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
                && !additionalBeginningPhaseUntap
                && gameData.additionalBeginningPhaseReturnStep != null) {
            if (gameData.additionalBeginningPhasesAfterCombat > 0) {
                next = TurnStep.UNTAP;
                gameData.additionalBeginningPhasesAfterCombat--;
            } else {
                next = gameData.additionalBeginningPhaseReturnStep;
                gameData.additionalBeginningPhaseReturnStep = null;
            }
        }

        next = skipChosenPhases(gameData, next);
        if (aggressiveCombat && next == TurnStep.BEGINNING_OF_COMBAT) {
            // Aggressive: "only creatures with aggressive may attack during it", until that combat ends (CR 511.3).
            gameData.addFloatingEffect(FloatingContinuousEffect.onPlayer("", null, gameData.activePlayerId,
                    new OnlyMatchingCreaturesCanAttackThisCombatEffect(new PermanentHasKeywordPredicate(Keyword.AGGRESSIVE)),
                    null, Duration.UNTIL_END_OF_COMBAT));
        }

        // An additional combat in which only a Throat Wolf can attack is its controller's combat, not
        // necessarily the player's whose combat just ended. Restore the original active player after the
        // inserted combat has finished.
        if (next == TurnStep.BEGINNING_OF_COMBAT && additionalCombatPhase) {
            UUID combatPlayerId = onlySourceCanAttackController(gameData, gameData.combatPhasesThisTurn + 1);
            if (combatPlayerId != null && !combatPlayerId.equals(gameData.activePlayerId)) {
                if (gameData.additionalCombatReturnActivePlayerId == null) {
                    gameData.additionalCombatReturnActivePlayerId = gameData.activePlayerId;
                }
                gameData.activePlayerId = combatPlayerId;
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

        if (next != null) {
            if (next == TurnStep.COMBAT_DAMAGE || next == TurnStep.END_OF_COMBAT) {
                gameData.firstCombatDamageTimingClosed = true;
            }
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

            if (next == TurnStep.DRAW) {
                gameData.drawStepFirstDrawTaken.clear();
            }
            if (next == TurnStep.UNTAP) {
                beginAdditionalBeginningPhase(gameData);
                return;
            }

            if (additionalBeginningPhaseUntap) {
                if (gameData.interaction.isAwaitingInput()) {
                    return;
                }
                processAdditionalBeginningPhaseUntap(gameData);
                return;
            }

            if (gameData.interaction.isAwaitingInput()) {
                return;
            }

            if (next == TurnStep.UPKEEP) {
                stepTriggerService.handleUpkeepTriggers(gameData);
            } else if (next == TurnStep.PRECOMBAT_MAIN) {
                stepTriggerService.handlePrecombatMainTriggers(gameData);
            } else if (next == TurnStep.POSTCOMBAT_MAIN) {
                stepTriggerService.handlePostcombatMainTriggers(gameData);
            } else if (next == TurnStep.DRAW) {
                stepTriggerService.handleDrawStep(gameData);
            } else if (next == TurnStep.BEGINNING_OF_COMBAT) {
                gameData.playersAttackedThisCombat.clear();
                gameData.combatPhasesThisTurn++;
                gameData.cardsPutIntoGraveyardThisCombat.clear();
                gameData.declaredAttackerIdsThisCombat.clear();
                gameData.creaturesThatEnlistedThisCombat.clear();
                gameData.forEachPermanent((playerId, p) -> {
                    p.setAttackedThisCombat(false);
                    p.setBlockedThisCombat(false);
                });
                gameData.combatBlockOpponentIdsThisCombat.clear();
                gameData.combatDamageToPlayersThisCombat.clear();
                stepTriggerService.handleBeginningOfCombatTriggers(gameData);
                logPlayerControlBegun(gameData, Duration.Start.AFFECTED_CONTROLLERS_NEXT_COMBAT);
            } else if (next == TurnStep.DECLARE_ATTACKERS) {
                stepTriggerService.handleDeclareAttackersTriggers(gameData);
                combatService.handleDeclareAttackersStep(gameData);
            } else if (next == TurnStep.DECLARE_BLOCKERS) {
                handleCombatResult(combatService.handleDeclareBlockersStep(gameData), gameData);
            } else if (next == TurnStep.COMBAT_DAMAGE) {
                handleCombatResult(combatService.resolveCombatDamage(gameData), gameData);
            } else if (next == TurnStep.END_OF_COMBAT) {
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

    /** Whether a Runed Terror effect is active; the board walk only reads, so it shares one query scope. */
    private boolean hasRunedTerror(GameData gameData) {
        java.util.function.Supplier<Boolean> scan = () -> gameData.orderedPlayerIds.stream()
                .map(gameData.playerBattlefields::get)
                .filter(java.util.Objects::nonNull)
                .flatMap(List::stream)
                .filter(permanent -> !gameQueryService.hasLostAllAbilities(gameData, permanent))
                .flatMap(permanent -> permanent.getCard().getStaticEffects().stream())
                .anyMatch(RunedTerrorEffect.class::isInstance);
        return gameQueryService == null ? scan.get() : gameQueryService.withQueryScope(gameData, scan);
    }

    private boolean advanceRunedTerrorPhase(GameData gameData) {
        if (gameData.currentStep != TurnStep.DRAW
                && gameData.currentStep != TurnStep.PRECOMBAT_MAIN
                && gameData.currentStep != TurnStep.END_OF_COMBAT
                && gameData.currentStep != TurnStep.POSTCOMBAT_MAIN
                && gameData.currentStep != TurnStep.CLEANUP) {
            return false;
        }

        if (!gameData.runedTerrorPhaseSequenceActive) {
            gameData.runedTerrorPhaseSequenceActive = true;
            gameData.runedTerrorPhaseSequenceFirstPlayerId = gameData.activePlayerId;
        }

        UUID firstPlayerId = gameData.runedTerrorPhaseSequenceFirstPlayerId;
        UUID nextPlayerId = nextPlayerInTurnOrder(gameData);
        if (gameData.currentStep == TurnStep.CLEANUP && firstPlayerId.equals(nextPlayerId)) {
            advanceTurn(gameData);
            return true;
        }

        boolean phaseComplete = firstPlayerId.equals(nextPlayerId);
        turnCleanupService.attributeEndOfTurnStateToActivePlayer(gameData);
        gameData.activePlayerId = phaseComplete ? firstPlayerId : nextPlayerId;
        TurnStep nextStep = nextRunedTerrorStep(gameData.currentStep, phaseComplete);
        gameData.resetPriorityToActivePlayer();
        gameData.interaction.clearAwaitingInput();
        gameData.currentStep = nextStep;
        gameData.currentUpkeepIsAdditional = false;
        turnCleanupService.drainManaPools(gameData);

        gameLogService.append(gameData, GameLog.text("Step: " + nextStep.getDisplayName()));
        log.info("Game {} - Runed Terror advanced to {} for {}", gameData.id, nextStep,
                gameData.playerIdToName.get(gameData.activePlayerId));
        invalidateForAllPlayers(gameData);

        if (gameData.status == GameStatus.FINISHED) {
            return true;
        }
        processRunedTerrorStepStart(gameData, nextStep);
        return true;
    }

    private TurnStep nextRunedTerrorStep(TurnStep currentStep, boolean phaseComplete) {
        return switch (currentStep) {
            case DRAW -> phaseComplete ? TurnStep.PRECOMBAT_MAIN : TurnStep.UNTAP;
            case PRECOMBAT_MAIN -> phaseComplete ? TurnStep.BEGINNING_OF_COMBAT : TurnStep.PRECOMBAT_MAIN;
            case END_OF_COMBAT -> phaseComplete ? TurnStep.POSTCOMBAT_MAIN : TurnStep.BEGINNING_OF_COMBAT;
            case POSTCOMBAT_MAIN -> phaseComplete ? TurnStep.END_STEP : TurnStep.POSTCOMBAT_MAIN;
            case CLEANUP -> TurnStep.END_STEP;
            default -> throw new IllegalArgumentException("Not a Runed Terror phase boundary: " + currentStep);
        };
    }

    private void processRunedTerrorStepStart(GameData gameData, TurnStep step) {
        if (step == TurnStep.UNTAP) {
            beginRunedTerrorBeginningPhase(gameData);
            return;
        }
        if (gameData.interaction.isAwaitingInput()) {
            return;
        }
        if (step == TurnStep.PRECOMBAT_MAIN) {
            stepTriggerService.handlePrecombatMainTriggers(gameData);
        } else if (step == TurnStep.BEGINNING_OF_COMBAT) {
            beginSequentialCombatPhase(gameData);
        } else if (step == TurnStep.POSTCOMBAT_MAIN) {
            stepTriggerService.handlePostcombatMainTriggers(gameData);
        } else if (step == TurnStep.END_STEP) {
            stepTriggerService.handleEndStepTriggers(gameData);
        }
    }

    /**
     * Logs the control of the active player that began just now, with a turn or a combat phase as {@code start} says
     * (CR 724.1), and returns whether one did. Control by a player who has left the game ends first.
     */
    private boolean logPlayerControlBegun(GameData gameData, Duration.Start start) {
        // A player who has left the game controls no one.
        gameData.expireFloatingEffects(effect -> effect.effect() instanceof ControlPlayerEffect control
                && !gameData.playerIds.contains(control.controllerPlayerId()));
        FloatingContinuousEffect control = gameData.playerControl();
        if (control == null || control.duration().start() != start
                || !gameData.activePlayerId.equals(control.affectedPlayerId())) {
            return false;
        }
        String controllerName = gameData.playerIdToName.get(gameData.mindControllerPlayerId());
        String controlledName = gameData.playerIdToName.get(gameData.activePlayerId);
        String when = start == Duration.Start.AFFECTED_PLAYERS_NEXT_TURN
                ? " this turn (Mindslaver)." : " during their next combat phase.";
        gameLogService.append(gameData, GameLog.text(controllerName + " controls " + controlledName + when));
        log.info("Game {} - {} controls {}{}", gameData.id, controllerName, controlledName, when);
        return true;
    }

    /**
     * Queues the extra turn each control effect that began with this turn promises after it (Emrakul, the Promised
     * End): it is created only once control actually begins.
     */
    private void queueExtraTurnsAfterControlledTurn(GameData gameData, UUID activePlayerId) {
        String activeName = gameData.playerIdToName.get(activePlayerId);
        for (FloatingContinuousEffect control : gameData.floatingEffectsOnNamedPlayers(ControlPlayerEffect.class)) {
            if (activePlayerId.equals(control.affectedPlayerId())
                    && control.duration().start() == Duration.Start.AFFECTED_PLAYERS_NEXT_TURN
                    && ((ControlPlayerEffect) control.effect()).extraTurnAfter()) {
                gameData.queueExtraTurnFirst(activePlayerId, false);
                gameLogService.append(gameData, GameLog.text(activeName + " takes an extra turn after this one."));
                log.info("Game {} - {} granted an extra turn after the controlled turn", gameData.id, activeName);
            }
        }
    }

    private void beginSequentialCombatPhase(GameData gameData) {
        gameData.playersAttackedThisCombat.clear();
        gameData.combatPhasesThisTurn++;
        gameData.cardsPutIntoGraveyardThisCombat.clear();
        gameData.declaredAttackerIdsThisCombat.clear();
        gameData.creaturesThatEnlistedThisCombat.clear();
        gameData.forEachPermanent((playerId, permanent) -> {
            permanent.setAttackedThisCombat(false);
            permanent.setBlockedThisCombat(false);
        });
        gameData.combatBlockOpponentIdsThisCombat.clear();
        gameData.combatDamageToPlayersThisCombat.clear();
        stepTriggerService.handleBeginningOfCombatTriggers(gameData);
        logPlayerControlBegun(gameData, Duration.Start.AFFECTED_CONTROLLERS_NEXT_COMBAT);
    }

    private void beginRunedTerrorBeginningPhase(GameData gameData) {
        gameData.runedTerrorSequentialUntapInProgress = true;
        UUID activePlayerId = gameData.activePlayerId;
        String activePlayerName = gameData.playerIdToName.get(activePlayerId);
        boolean skipUntapStep = false;
        if (gameData.applySkipNext(activePlayerId, SkipKind.UNTAP_STEP)) {
            skipUntapStep = true;
            gameLogService.append(gameData, GameLog.text(activePlayerName + " skips their untap step."));
        }

        if (skipUntapStep || untapStepService.playersSkipUntapStepApplies(gameData)) {
            untapStepService.untapPermanents(gameData, activePlayerId, null, true);
        } else if (untapStepService.phaseThenCheckStorageMatrixRestriction(gameData, activePlayerId)) {
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
        completeTurnAdvance(gameData);
    }

    /**
     * The controller of an "only this creature can attack during that combat" effect waiting for this turn's
     * {@code combatPhase}-th combat phase, or {@code null} if there is none.
     */
    private static UUID onlySourceCanAttackController(GameData gameData, int combatPhase) {
        synchronized (gameData.scheduledFloatingEffects) {
            return gameData.scheduledFloatingEffects.stream()
                    .filter(effect -> effect.effect() instanceof OnlySourceCreatureCanAttackThisCombatEffect
                            && effect.duration().start() == Duration.Start.COMBAT_PHASE_OF_THIS_TURN
                            && effect.duration().combatPhase() == combatPhase)
                    .map(FloatingContinuousEffect::controllerId)
                    .findFirst().orElse(null);
        }
    }

    private void processAdditionalBeginningPhaseUntap(GameData gameData) {
        UUID activePlayerId = gameData.activePlayerId;
        // Savor the Moment's extra turn skips this additional untap step too (see
        // GameData.extraTurnSkipsAllUntapSteps); a queued Yosei skip is not consumed by it.
        boolean skipUntapStep = untapStepService.playersSkipUntapStepApplies(gameData)
                || gameData.extraTurnSkipsAllUntapSteps;
        if (!skipUntapStep && gameData.applySkipNext(activePlayerId, SkipKind.UNTAP_STEP)) {
            skipUntapStep = true;
        }

        if (skipUntapStep) {
            untapStepService.untapPermanents(gameData, activePlayerId, null, true);
        } else if (untapStepService.phaseThenCheckStorageMatrixRestriction(gameData, activePlayerId)) {
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
        completeTurnAdvance(gameData);
    }

    private TurnStep skipChosenPhases(GameData gameData, TurnStep next) {
        while (next != null) {
            // Apply the queued skip at the combat boundary, including when a skipped main
            // phase brought us here without ever entering PRECOMBAT_MAIN.
            if (next == TurnStep.BEGINNING_OF_COMBAT && consumeQueuedCombatPhaseSkip(gameData)) {
                gameData.firstCombatDamageTimingClosed = true;
                logSkippedPhase(gameData, "combat phase");
                if (gameData.additionalCombatPhasesOnly > 0) {
                    // Only the upcoming combat is skipped; the extra combat phase queued after it still happens.
                    gameData.additionalCombatPhasesOnly--;
                    if (gameData.aggressiveCombatPhasesOnly > 0) {
                        gameData.aggressiveCombatPhasesOnly--;
                    }
                    next = TurnStep.BEGINNING_OF_COMBAT;
                } else {
                    next = TurnStep.POSTCOMBAT_MAIN;
                }
                continue;
            }
            if (next == TurnStep.PRECOMBAT_MAIN && skips(gameData, SkipStepOrPhaseKind.MAIN_PHASE)) {
                logSkippedPhase(gameData, "main phase");
                next = TurnStep.BEGINNING_OF_COMBAT;
                continue;
            }
            if ((next == TurnStep.BEGINNING_OF_COMBAT || next == TurnStep.END_OF_COMBAT)
                    && skips(gameData, SkipStepOrPhaseKind.COMBAT_PHASE)) {
                gameData.firstCombatDamageTimingClosed = true;
                logSkippedPhase(gameData, "combat phase");
                if (gameData.additionalCombatPhasesOnly > 0) {
                    gameData.additionalCombatPhasesOnly--;
                    if (gameData.aggressiveCombatPhasesOnly > 0) {
                        gameData.aggressiveCombatPhasesOnly--;
                    }
                    next = TurnStep.BEGINNING_OF_COMBAT;
                } else {
                    next = TurnStep.POSTCOMBAT_MAIN;
                }
                continue;
            }
            if (next == TurnStep.POSTCOMBAT_MAIN && skips(gameData, SkipStepOrPhaseKind.MAIN_PHASE)) {
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

    private static boolean skips(GameData gameData, SkipStepOrPhaseKind kind) {
        return gameData.skipsStepOrPhase(gameData.activePlayerId, kind);
    }

    /**
     * Uses up the active player's queued skip of their next combat phase (CR 614.10a): one that lasts only this turn
     * first, then one that lasts until it is used.
     *
     * @return whether a skip was used
     */
    private static boolean consumeQueuedCombatPhaseSkip(GameData gameData) {
        UUID activePlayerId = gameData.activePlayerId;
        return gameData.applySkipNext(activePlayerId, SkipKind.COMBAT_PHASE);
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
        advanceTurn(gameData, true, true);
    }

    private void advanceTurn(GameData gameData, boolean snapshotEndingPlayer) {
        advanceTurn(gameData, snapshotEndingPlayer, true);
    }

    /** Completes Time Vault's turn-start replacement choice and resumes turn progression. */
    public void completeTimeVaultChoice(GameData gameData, UUID controllerId,
                                         UUID sourcePermanentId, boolean accepted) {
        if (accepted) {
            Permanent timeVault = gameQueryService.findPermanentById(gameData, sourcePermanentId);
            gameData.addFloatingEffect(FloatingContinuousEffect.onPlayer(
                    timeVault == null ? "Time Vault" : FaceDownVisibility.publicName(gameData, timeVault), sourcePermanentId, controllerId,
                    new SkipNextEffect(SkipKind.TURN), controllerId, Duration.PERMANENT));
            advanceTurn(gameData, false, false);
        } else {
            gameData.timeVaultsDeclinedThisTurnStart.add(sourcePermanentId);
            advanceTurn(gameData, false, true);
        }
    }

    private void advanceTurn(GameData gameData, boolean snapshotEndingPlayer,
                             boolean offerTimeVaultChoice) {
        if (snapshotEndingPlayer) {
            gameData.snapshotPlayerActionsForLastTurn(gameData.activePlayerId);
            gameData.snapshotPlayerAttacksForLastTurn(gameData.activePlayerId);
        }
        gameData.snapshotPlayerAttacksForLastTurn(gameData.activePlayerId);
        UUID endingActivePlayerId = gameData.activePlayerId;
        gameData.forEachPermanent((controllerId, permanent) -> permanent.recordTurnEnded(endingActivePlayerId));
        gameData.phasedOutPermanents.values().forEach(
                battlefield -> battlefield.forEach(permanent -> permanent.recordTurnEnded(endingActivePlayerId)));

        UUID nextActive;
        Long extraTurnSequence = null;
        boolean currentTurnIsExtraTurn = false;
        boolean skipUntapStep = false;
        boolean powerUpAbilitiesDisabled = false;
        boolean damageCantBePrevented = false;
        if (!gameData.currentTurnIsExtraTurn) gameData.lastNormalTurnPlayerId = gameData.activePlayerId;
        UUID nextTurnPlayer = !gameData.extraTurns.isEmpty()
                ? gameData.extraTurns.peekFirst()
                : nextPlayerInTurnOrder(gameData);
        if (offerTimeVaultChoice) {
            Permanent timeVault = findTappedTimeVault(gameData, nextTurnPlayer,
                    gameData.timeVaultsDeclinedThisTurnStart);
            if (timeVault != null) {
                queueTimeVaultChoice(gameData, nextTurnPlayer, timeVault);
                return;
            }
        }
        gameData.timeVaultsDeclinedThisTurnStart.clear();
        if (!gameData.extraTurns.isEmpty()) {
            nextActive = gameData.extraTurns.pollFirst();
            currentTurnIsExtraTurn = true;
            skipUntapStep = Boolean.TRUE.equals(gameData.extraTurnSkipsUntap.pollFirst());
            powerUpAbilitiesDisabled = Boolean.TRUE.equals(gameData.extraTurnPowerUpAbilitiesDisabled.pollFirst());
            damageCantBePrevented = Boolean.TRUE.equals(gameData.extraTurnDamageCantBePrevented.pollFirst());
            extraTurnSequence = gameData.extraTurnSequences.isEmpty()
                    ? null : gameData.extraTurnSequences.pollFirst();
            if (hasApplicableExtraTurnSkipReplacement(gameData, nextActive)) {
                if (extraTurnSequence != null) {
                    gameData.endDelayedAbilitiesOfExtraTurn(extraTurnSequence);
                }
                String skippedName = gameData.playerIdToName.get(nextActive);
                gameLogService.append(gameData, GameLog.text(skippedName + " skips their extra turn."));
                log.info("Game {} - {} skips their extra turn", gameData.id, skippedName);
                advanceTurn(gameData, false);
                return;
            }
        } else {
            nextActive = nextTurnPlayer;
        }

        // Chronatog: skip the turn entirely (CR 500.11 / 614.10). Pending Mindslaver waits (CR 724.1b).
        if (gameData.applySkipNext(nextActive, SkipKind.TURN)) {
            String skippedName = gameData.playerIdToName.get(nextActive);
            if (currentTurnIsExtraTurn && extraTurnSequence != null) {
                gameData.endDelayedAbilitiesOfExtraTurn(extraTurnSequence);
            }
            gameLogService.append(gameData, GameLog.text(skippedName + " skips their turn."));
            log.info("Game {} - {} skips their turn", gameData.id, skippedName);
            // Advance turn order past the skipped player so the next selection is correct.
            gameData.activePlayerId = nextActive;
            gameData.currentTurnIsExtraTurn = currentTurnIsExtraTurn;
            if (!currentTurnIsExtraTurn) gameData.lastNormalTurnPlayerId = nextActive;
            if (gameData.planechase != null) gameData.planechase.controllerId = nextActive;
            advanceTurn(gameData, false);
            return;
        }

        String nextActiveName = gameData.playerIdToName.get(nextActive);
        gameData.currentTurnIsExtraTurn = currentTurnIsExtraTurn;
        if (powerUpAbilitiesDisabled) {
            gameData.addFloatingEffect(FloatingContinuousEffect.onPlayer("", null, nextActive,
                    new PowerUpAbilitiesCantBeActivatedEffect(), null, Duration.UNTIL_END_OF_TURN));
        }
        if (damageCantBePrevented) {
            gameData.addFloatingEffect(FloatingContinuousEffect.onPlayer("", null, nextActive,
                    new DamageCantBePreventedThisTurnEffect(), null, Duration.UNTIL_END_OF_TURN));
        }
        gameData.currentExtraTurnSequence = extraTurnSequence;
        gameData.extraTurnSkipsAllUntapSteps = skipUntapStep;

        // Yosei, the Morning Star: a queued "skips their next untap step" is consumed by the first
        // untap step this player would actually get (CR 614.10a). A turn whose untap step is already
        // skipped by an extra-turn skip does not consume the queued skip; it waits for the next one.
        if (!skipUntapStep && gameData.applySkipNext(nextActive, SkipKind.UNTAP_STEP)) {
            skipUntapStep = true;
            gameLogService.append(gameData, GameLog.text(nextActiveName + " skips their untap step."));
            log.info("Game {} - {} skips their untap step", gameData.id, nextActiveName);
        }

        gameData.previousTurnActivePlayerId = gameData.activePlayerId;
        gameData.activePlayerId = nextActive;
        gameData.turnStartTimestamp = gameData.timestampCounter + 1;
        if (gameData.planechase != null) gameData.planechase.controllerId = nextActive;
        gameData.creaturesAbleToAttackAtDeclareAttackersThisTurn.clear();

        gameData.turnNumber++;
        gameData.mainPhasesBegunThisTurn = 0;
        gameData.postcombatMainPhasesBegunThisTurn = 0;
        gameData.temporaryGlobalTriggeredAbilities.removeIf(watcher ->
                watcher.untilNextTurn()
                        && nextActive.equals(watcher.expirationPlayerId() != null
                        ? watcher.expirationPlayerId() : watcher.controllerId())
                        && gameData.turnNumber != watcher.registrationTurnNumber());
        gameData.cardPutIntoExileThisTurn = false;
        gameData.pileGroupingOrGuessCountThisTurn = 0;
        gameData.turnsTakenByPlayer.merge(nextActive, 1, Integer::sum);
        gameData.currentStep = TurnStep.first();
        gameData.additionalUpkeepsRemaining = 0;
        gameData.currentUpkeepIsAdditional = false;
        gameData.interaction.clearAwaitingInput();
        gameData.resetPriorityToActivePlayer();
        gameData.playersWhoWereWayBehindThisTurn.clear();
        gameData.landsPlayedThisTurn.clear();
        gameData.landCardsPlayedThisTurn.clear();
        gameData.playersWhoTappedLandForManaThisTurn.clear();
        gameData.additionalLandsThisTurn.clear();
        gameData.permanentsEnteredSincePlayerLastTurnEnded.remove(endingActivePlayerId);
        gameData.permanentsEnteredBattlefieldLastTurn.clear();
        gameData.permanentsEnteredBattlefieldThisTurn.forEach((playerId, entered) ->
                gameData.permanentsEnteredBattlefieldLastTurn.put(playerId, new ArrayList<>(entered)));
        gameData.permanentsEnteredBattlefieldThisTurn.clear();
        gameData.faceDownCreaturesEnteredBattlefieldThisTurn.clear();
        gameData.faceDownPermanentsEnteredBattlefieldThisTurn.clear();
        gameData.playersWhoTurnedPermanentsFaceUpThisTurn.clear();
        gameData.energyCountersPaidOrLostThisTurn.clear();
        gameData.permanentsTurnedFaceUpThisTurn.clear();
        gameData.snapshotSpellCountsAndClear(gameData.spellsCastLastTurn);
        gameData.clearGreatestStackSourceCountThisTurn();
        gameData.crimeCandidatesThisTurn.clear();
        gameData.clearSpellsCastFromHandThisTurn();
        gameData.clearSpellsCastFromOutsideHandThisTurn();
        gameData.playersWhoSearchedLibraryThisTurn.clear();
        gameData.playersWhoInvestigatedThisTurn.clear();
        gameData.playersWhoVenturedIntoDungeonThisTurn.clear();
        gameData.sacrificedPermanentSubtypeCountThisTurn.clear();
        gameData.sacrificedPermanentCountThisTurn.clear();
        gameData.permanentsSacrificedThisTurn.clear();
        gameData.playersWhoSurveilledThisTurn.clear();
        gameData.playersWhoControlledPermanentThatExploredThisTurn.clear();
        gameData.playersWhoFlippedCoinsThisTurn.clear();
        gameData.permanentTypesCastFromGraveyardThisTurn.clear();
        gameData.playersWhoActivatedAbilityOfGraveyardCardThisTurn.clear();
        gameData.oncePerTurnGraveyardCastPermissionsUsedThisTurn.clear();
        gameData.oncePerTurnGraveyardLandPermissionsUsedThisTurn.clear();
        gameData.oncePerTurnGraveyardSpellPermissionsUsedThisTurn.clear();
        gameData.playersDeclaredAttackersThisTurn.clear();
        gameData.playersWhoAttackedWithTokenThisTurn.clear();
        gameData.playersWhoAttackedWithCommanderThisTurn.clear();
        gameData.playersWhoPutCountersOnCreaturesThisTurn.clear();
        gameData.permanentsWithCountersPutByPlayerThisTurn.clear();
        gameData.playersWhoPutPlusOnePlusOneCountersOnCreaturesThisTurn.clear();
        gameData.playersWhoRemovedOilCountersFromControlledPermanentsThisTurn.clear();
        gameData.permanentWithOilCounterPutIntoGraveyardThisTurn = false;
        gameData.artifactOrCreaturePutIntoGraveyardFromBattlefieldThisTurn = false;
        gameData.permanentPutIntoGraveyardFromBattlefieldThisTurn = false;
        gameData.playersWhoPutEnchantmentIntoGraveyardFromBattlefieldThisTurn.clear();
        gameData.playersWhoControlledLandPutIntoGraveyardFromBattlefieldThisTurn.clear();
        gameData.permanentsPutIntoGraveyardFromBattlefieldThisTurn = 0;
        gameData.playersWhoControlledPermanentsThatReceivedPlusOneCountersThisTurn.clear();
        gameData.playersWhoCreatedTokensThisTurn.clear();
        gameData.treasureTokensCreatedThisTurn.clear();
        gameData.permanentsThatFoughtThisTurn.clear();
        gameData.tokensCreatedThisTurn.clear();
        gameData.playersWhoSacrificedPermanentsThisTurn.clear();
        gameData.playersWhoSacrificedArtifactsThisTurn.clear();
        gameData.creaturesAttackedCountThisTurn.clear();
        gameData.creaturesAttackedWithThisTurn.clear();
        gameData.creaturesAttackedCountBySubtypeThisTurn.clear();
        gameData.permanentsThatAttackedBattlesThisTurn.clear();
        gameData.activatedAbilityUsesThisTurn.clear();
        gameData.playersWhoUsedMaxSpeedFreeUnearthThisTurn.clear();
        gameData.playersWhoActivatedExhaustAbilityThisTurn.clear();
        gameData.playersWhoActivatedEquipAbilityThisTurn.clear();
        gameData.playersWhoActivatedArtifactAbilityThisTurn.clear();
        gameData.playersWhoActivatedPowerUpAbilityThisTurn.clear();
        gameData.playersWhoActivatedLoyaltyAbilityThisTurn.clear();
        gameData.playersWhoActivatedSparkAbilityThisTurn.clear();
        gameData.permanentAbilityResolutionsThisTurn.clear();
        gameData.creatureCardsPutIntoGraveyardFromBattlefieldThisTurn.clear();
        gameData.cardsPutIntoGraveyardFromBattlefieldThisTurn.clear();
        gameData.cardsExiledFromGraveyardThisTurn.clear();
        gameData.artifactsPutIntoGraveyardFromBattlefieldThisTurn = 0;
        gameData.cardsPutIntoGraveyardFromAnywhereThisTurn.clear();
        gameData.cardsPutIntoGraveyardAgainThisTurn.clear();
        gameData.cardsPutIntoGraveyardFromLibraryThisTurn.clear();
        gameData.cardsMilledThisTurn.clear();
        gameData.cardsSurveilledThisTurn.clear();
        gameData.cardsPutIntoHandThisTurn.clear();
        gameData.cardsPutIntoGraveyardFromHandThisTurn.clear();
        gameData.cardsPutIntoGraveyardFromHandOrLibraryThisTurn.clear();
        gameData.creatureCardsPutIntoGraveyardFromAnywhereThisTurn.clear();
        gameData.creatureCardGraveyardEntriesThisTurn.clear();
        gameData.landCardGraveyardEntriesThisTurn.clear();
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
        gameData.creaturePermanentIdsDiedThisTurn.clear();
        gameData.playersWhoControlledModifiedCreatureDiedThisTurn.clear();
        gameData.creaturesPutIntoOwnGraveyardThisTurnCount.clear();
        gameData.nontokenCreaturesPutIntoOwnGraveyardThisTurnCount.clear();
        gameData.nontokenCreatureDeathCountThisTurn.clear();
        gameData.creatureSubtypeDeathCountThisTurn.clear();
        gameData.creatureSubtypeDeathPowerThisTurn.clear();
        gameData.cardsDrawnLastTurn.clear();
        gameData.cardsDrawnLastTurn.putAll(gameData.cardsDrawnThisTurn);
        gameData.cardsDrawnThisTurn.clear();
        gameData.cardsDrawnThisTurnIds.clear();
        gameData.cardsDiscardedThisTurn.clear();
        gameData.nonlandCardsDiscardedThisTurn.clear();
        gameData.cardsCycledThisTurn.clear();
        gameData.lifeGainedThisTurn.clear();
        gameData.lifeLostLastTurn.clear();
        gameData.lifeLostLastTurn.putAll(gameData.lifeLostThisTurn);
        gameData.lifeLostThisTurn.clear();
        gameData.playersDealtCombatDamageLastTurn.clear();
        if (gameData.playersDealtCombatDamageSinceTheirLastTurn.remove(nextActive)) {
            gameData.playersDealtCombatDamageLastTurn.add(nextActive);
        }
        gameData.permanentDamageSourceNamesToPlayersLastTurn.clear();
        gameData.permanentDamageSourceNamesToPlayersThisTurn.forEach((playerId, sourceNames) ->
                gameData.permanentDamageSourceNamesToPlayersLastTurn.put(playerId, new HashSet<>(sourceNames)));
        gameData.combatDamageToPlayersThisTurn.clear();
        gameData.combatDamageDealtToPlayersThisTurn.clear();
        gameData.combatDamageSourcesThatDealtToCreaturesThisTurn.clear();
        gameData.noncombatDamageToPlayersThisTurn.clear();
        gameData.permanentDamageSourceNamesToPlayersThisTurn.clear();
        gameData.creatureDamageToPlayersThisTurn.clear();
        gameData.damageDealtThisTurnBySource.clear();
        gameData.sourcePermanentsThatDealtDamageToCreaturesThisTurn.clear();
        gameData.damageDealtToPlayersBySourceThisTurn.clear();
        gameData.sorcerySpellDamageDealtThisTurn.clear();
        gameData.damageSourcesControlledByPlayerThisTurn.clear();
        gameData.playersAttackedThisTurn.clear();
        gameData.declaredAttackerSnapshotsThisTurn.clear();
        gameData.playersAfraidOfControllerAfterAttackThisTurn.clear();
        gameData.playersAttackedThisCombat.clear();
        gameData.playersWhoAttackedPlayerOrPlaneswalkerThisTurn.clear();
        gameData.playersWhoAttackedPlayersThisTurn.clear();
        gameData.creaturesThatSaddledPermanentThisTurn.clear();
        gameData.creaturesThatCrewedPermanentThisTurn.clear();
        gameData.crewedPermanentSubtypesThisTurn.clear();
        gameData.combatDamageSourceSubtypesThisTurn.clear();
        gameData.combatDamageSourceNamesThisTurn.clear();
        gameData.combatDamageSourcesWithChangelingThisTurn.clear();
        gameData.combatDamageSourcesWithLegendaryThisTurn.clear();
        gameData.legendaryCreatureCombatDamageToPlayersThisTurn.clear();
        gameData.combatDamageToPlayerControllerSubtypesThisTurn.clear();
        gameData.combatDamageDealersByControllerThisTurn.clear();
        gameData.controllersDealtCombatDamageWithChangelingThisTurn.clear();
        gameData.combatDamageSourcesThatWereCommandersThisTurn.clear();
        gameData.combatBlockOpponentSubtypesThisTurn.clear();
        gameData.combatBlockOpponentColorsThisTurn.clear();
        gameData.creaturesInCombatWithChangelingThisTurn.clear();
        gameData.combatBlockOpponentIdsThisTurn.clear();
        gameData.combatBlockOpponentControllerIdsThisTurn.clear();
        gameData.combatOpponentIdsBlockedByThisTurn.clear();
        gameData.creaturesBlockedThisTurn.clear();
        gameData.creaturesEnteredBlockingThisTurn.clear();
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
        gameData.damageDealtToPermanentsBySourceControllerThisTurn.clear();
        gameData.damageSourceNamesThisTurn.clear();
        gameData.controllersOfPermanentsDealtExcessDamageThisTurn.clear();
        gameData.qualifyingDamageControllersByPermanentThisTurn.clear();
        gameData.freeCastPermanentUsedThisTurn.clear();
        gameData.oncePerTurnExileCastPermissionsUsedThisTurn.clear();
        gameData.oncePerTurnLibraryCastPermissionsUsedThisTurn.clear();
        gameData.oncePerTurnLibraryPlayPermissionsUsedThisTurn.clear();
        gameData.permanentsWithCountersPutThisTurn.clear();
        gameData.departedPermanentSnapshots.clear();
        gameData.departedPermanentControllerIds.clear();
        gameData.firstCardCycledFreeUsesThisTurn.clear();
        gameData.firstNonDrawStepDrawReplacementsUsedThisTurn.clear();
        gameData.keyedOncePerTurnTriggersFiredThisTurn.clear();
        gameData.oncePerCreatureTriggersFiredThisTurn.clear();
        gameData.creaturesThatReceivedCountersThisTurn.clear();
        gameData.creatureTapCountsThisTurn.clear();
        gameData.permanentsThatAddedManaWithAbilityThisTurn.clear();
        gameData.firstResolutionTriggerKeysThisTurn.clear();
        gameData.permanentsThatReceivedPlusOnePlusOneCountersThisTurn.clear();
        gameData.permanentsWithPlusOneCountersPutByPlayerThisTurn.clear();
        gameData.plusOnePlusOneCountersPutOnControlledCreaturesThisTurn.clear();
        gameData.permanentsExertedThisTurn.clear();
        gameData.bendingTypesCompletedThisTurn.clear();
        gameData.tokenCreationReplacementUsedThisTurn.clear();
        gameData.creatureCardsDamagedThisTurnBySourcePermanent.clear();
        gameData.creatureCardsDamagedThisTurnBySource.clear();
        gameData.creatureCardsDamagedThisTurnByController.clear();
        gameData.sourcesThatDealtDamageToCreaturesThisTurn.clear();
        gameData.sourcesWhoseDamagedCreaturesDiedThisTurn.clear();
        gameData.creatureCardsDamagedBySourceThatDiedThisTurn.clear();
        gameData.playersAffectedByMeliraPoisonReplacementThisTurn.clear();
        gameData.additionalCombatMainPhasePairs = 0;
        gameData.additionalCombatMainPhasePairsReturnStep = null;
        gameData.additionalCombatMainPhasePairsAfterSecondMain = 0;
        gameData.additionalCombatPhasesOnly = 0;
        gameData.aggressiveCombatPhasesOnly = 0;
        gameData.additionalCombatReturnActivePlayerId = null;
        gameData.additionalCombatPhasesAfterMain = 0;
        gameData.additionalCombatPhasesAfterMainReturnStep = null;
        gameData.additionalUpkeepStepsAfterCombat = 0;
        gameData.additionalUpkeepReturnStep = null;
        gameData.additionalBeginningPhasesAfterCombat = 0;
        gameData.additionalBeginningPhasesBeforeQueuedCombat = 0;
        gameData.additionalBeginningPhaseReturnStep = null;
        gameData.additionalBeginningPhaseUntapInProgress = false;
        gameData.combatPhasesThisTurn = 0;
        gameData.firstCombatDamageTimingClosed = false;
        gameData.endStepsThisTurn = 0;
        gameData.additionalEndStepsPending = 0;
        gameData.cleanupDiscardPending = false;
        gameData.paidSearchTaxPermanentIds.clear();

        turnCleanupService.drainManaPools(gameData);

        gameData.forEachPermanent((playerId, p) -> {
            p.setAttackedThisTurn(false);
            p.setAttacksThisTurn(0);
            p.setBlockedThisTurn(false);
            p.setBecomeTargetCounterUsedThisTurn(false);
            p.getChosenModeLabelsThisTurn().clear();
            p.getPermanentIdsThatDealtDamageThisTurn().clear();
        });
        gameData.phasedOutPermanents.values().forEach(
                battlefield -> battlefield.forEach(p -> {
                    p.getChosenModeLabelsThisTurn().clear();
                    p.getPermanentIdsThatDealtDamageThisTurn().clear();
                }));

        // The turn begins: the "until your next turn" effects of the player whose turn it is end.
        gameData.rulesEvents.occur(new TurnEvent.Began(nextActive));
        if (logPlayerControlBegun(gameData, Duration.Start.AFFECTED_PLAYERS_NEXT_TURN)) {
            queueExtraTurnsAfterControlledTurn(gameData, nextActive);
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
            if (untapStepService.phaseThenCheckStorageMatrixRestriction(gameData, nextActive)) {
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
                if (gameQueryService.hasLostPrintedAbilities(gameData, permanent)) continue;
                boolean applies = permanent.getCard().getStaticEffects().stream()
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
        var restriction = untapStepService.bindingUntapRestriction(gameData, activePlayerId);
        if (restriction.isPresent()) {
            var effect = restriction.get();
            var candidates = untapStepService.staticOrbUntapCandidates(
                    gameData, activePlayerId, effect, restrictPredicate);
            if (candidates.size() > effect.maxUntap()) {
                // Permanents outside Storage Matrix's chosen type must stay tapped as well.
                var combinedFilter = effect.filter() == null ? null
                        : new com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate(List.of(
                                new com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate(restrictPredicate),
                                effect.filter()));
                playerInputService.beginStaticOrbUntapChoice(gameData, activePlayerId,
                        candidates, effect.maxUntap(), combinedFilter);
                return;
            }
        }
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
        resumeStaticOrbUntap(gameData, activePlayerId, chosenUntapIds, staticOrbFilter, List.of(), List.of());
    }

    public void resumeStaticOrbUntap(GameData gameData, UUID activePlayerId,
            java.util.Set<UUID> chosenUntapIds,
            com.github.laxika.magicalvibes.model.filter.PermanentPredicate staticOrbFilter,
            List<com.github.laxika.magicalvibes.model.effect.StaticOrbEffect> remainingRestrictions,
            List<UUID> previouslyExcludedIds) {
        var excluded = new java.util.LinkedHashSet<>(previouslyExcludedIds);
        // Every binding cap was satisfied by the one choice, so the tapped permanents any of the caps
        // covers untap only when chosen; permanents none of them covers untap normally.
        java.util.function.Predicate<Permanent> cappedByAnyRestriction = permanent ->
                staticOrbFilter == null || predicateEvaluationService.matchesPermanentPredicate(
                        gameData, permanent, staticOrbFilter)
                        || remainingRestrictions.stream().anyMatch(restriction -> restriction.filter() == null
                        || predicateEvaluationService.matchesPermanentPredicate(
                        gameData, permanent, restriction.filter()));
        gameData.playerBattlefields.getOrDefault(activePlayerId, List.of()).stream()
                .filter(permanent -> permanent.isTapped() && cappedByAnyRestriction.test(permanent))
                .map(Permanent::getId).filter(id -> !chosenUntapIds.contains(id)).forEach(excluded::add);
        var excludedPredicate = new com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate(
                excluded.stream().map(id -> (com.github.laxika.magicalvibes.model.filter.PermanentPredicate)
                        new com.github.laxika.magicalvibes.model.filter.PermanentIsSpecificPermanentPredicate(id)).toList());
        untapStepService.untapPermanents(gameData, activePlayerId,
                excluded.isEmpty() ? null : new com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate(excludedPredicate));

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
        // Savor the Moment's extra turn skips this additional untap step too (see
        // GameData.extraTurnSkipsAllUntapSteps); a queued Yosei skip then waits for a later untap step.
        boolean skipUntapStep = gameData.extraTurnSkipsAllUntapSteps;
        if (!skipUntapStep && gameData.applySkipNext(activePlayerId, SkipKind.UNTAP_STEP)) {
            skipUntapStep = true;
            gameLogService.append(gameData, GameLog.text(activePlayerName + " skips their untap step."));
        }

        if (skipUntapStep || untapStepService.playersSkipUntapStepApplies(gameData)) {
            untapStepService.untapPermanents(gameData, activePlayerId, null, true);
        } else if (untapStepService.phaseThenCheckStorageMatrixRestriction(gameData, activePlayerId)) {
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
        if (gameData.runedTerrorSequentialUntapInProgress) {
            gameData.runedTerrorSequentialUntapInProgress = false;
            String activeName = gameData.playerIdToName.get(gameData.activePlayerId);
            gameLogService.append(gameData, GameLog.text(activeName + " begins their beginning phase."));
            invalidateForAllPlayers(gameData);
            return;
        }
        if (gameData.currentStep == TurnStep.UNTAP
                && gameData.additionalBeginningPhaseReturnStep != null) {
            invalidateForAllPlayers(gameData);
            advanceStep(gameData);
            return;
        }
        String activeName = gameData.playerIdToName.get(gameData.activePlayerId);
        String logEntry = "Turn " + gameData.turnNumber + " begins. " + activeName + "'s turn.";
        gameLogService.append(gameData, GameLog.text(logEntry));
        log.info("Game {} - Turn {} begins. Active player: {}", gameData.id, gameData.turnNumber, activeName);
        invalidateForAllPlayers(gameData);
        if (gameData.currentStep == TurnStep.UNTAP) {
            advanceStep(gameData);
        }
    }

    /** Restarts the current turn from its untap step after a turn-start snapshot was restored. */
    public void restartTurnFromBeginning(GameData gameData) {
        gameData.currentStep = TurnStep.UNTAP;
        gameData.resetPriorityToActivePlayer();
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

    public void processNextPucasMischiefTarget(GameData gameData) {
        stepTriggerService.processNextPucasMischiefTarget(gameData);
    }

    private UUID nextPlayerInTurnOrder(GameData gameData) {
        List<UUID> ids = new ArrayList<>(gameData.orderedPlayerIds);
        UUID currentActive = gameData.currentTurnIsExtraTurn && gameData.lastNormalTurnPlayerId != null
                ? gameData.lastNormalTurnPlayerId : gameData.activePlayerId;
        int currentIndex = ids.indexOf(currentActive);
        if (currentIndex < 0 || ids.isEmpty()) {
            return currentActive;
        }
        return ids.get((currentIndex + 1) % ids.size());
    }

    private Permanent findTappedTimeVault(GameData gameData, UUID controllerId, Set<UUID> excludedPermanentIds) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return null;
        }
        return battlefield.stream()
                .filter(permanent -> !excludedPermanentIds.contains(permanent.getId()))
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

    private void invalidateForAllPlayers(GameData gameData) {
        mutationCoordinator.emit(gameData,
                new GameEventFact.StateInvalidated(
                        GameEventFact.StateSection.TURN_AND_PRIORITY),
                GameEventAudience.allPlayers());
    }
}
