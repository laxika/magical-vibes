package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.model.AiDifficulty;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.GameActionAvailabilityService;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.service.GameSetupService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.cast.CastingCostService;
import com.github.laxika.magicalvibes.service.cast.CastingPermissionService;
import com.github.laxika.magicalvibes.service.combat.attack.CombatAttackService;
import com.github.laxika.magicalvibes.service.effect.TargetValidationService;
import com.github.laxika.magicalvibes.service.event.GameMutationCoordinator;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import com.github.laxika.magicalvibes.service.GameRegistry;
import com.github.laxika.magicalvibes.ai.simulation.MCTSEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class AiPlayerService {

    private final GameRegistry gameRegistry;
    private final GameService gameService;
    private final GameSetupService gameSetupService;
    private final GameQueryService gameQueryService;
    private final CombatAttackService combatAttackService;
    private final GameActionAvailabilityService actionAvailabilityService;
    private final CastingCostService castingCostService;
    private final CastingPermissionService castingPermissionService;
    private final TargetValidationService targetValidationService;
    private final TargetLegalityService targetLegalityService;
    private final GameMutationCoordinator mutationCoordinator;
    private final AiDecisionEventSubscriber decisionEventSubscriber;
    private final long mctsTimeBudgetMs;
    private final int mctsParallelism;

    public AiPlayerService(GameRegistry gameRegistry,
                           GameService gameService,
                           GameSetupService gameSetupService,
                           GameQueryService gameQueryService,
                           CombatAttackService combatAttackService,
                           GameActionAvailabilityService actionAvailabilityService,
                           CastingCostService castingCostService,
                           CastingPermissionService castingPermissionService,
                           TargetValidationService targetValidationService,
                           TargetLegalityService targetLegalityService,
                           GameMutationCoordinator mutationCoordinator,
                           AiDecisionEventSubscriber decisionEventSubscriber,
                           @Value("${ai.mcts.time-budget-ms:" + MCTSEngine.DEFAULT_TIME_BUDGET_MS + "}") long mctsTimeBudgetMs,
                           @Value("${ai.mcts.parallelism:0}") int mctsParallelism) {
        this.gameRegistry = gameRegistry;
        this.gameService = gameService;
        this.gameSetupService = gameSetupService;
        this.gameQueryService = gameQueryService;
        this.combatAttackService = combatAttackService;
        this.actionAvailabilityService = actionAvailabilityService;
        this.castingCostService = castingCostService;
        this.castingPermissionService = castingPermissionService;
        this.targetValidationService = targetValidationService;
        this.targetLegalityService = targetLegalityService;
        this.mutationCoordinator = mutationCoordinator;
        this.decisionEventSubscriber = decisionEventSubscriber;
        this.mctsTimeBudgetMs = mctsTimeBudgetMs;
        this.mctsParallelism = mctsParallelism;
    }

    public void joinAsAi(GameData gameData, String aiDeckId) {
        joinAsAi(gameData, aiDeckId, AiDifficulty.EASY);
    }

    public void joinAsAi(GameData gameData, String aiDeckId, AiDifficulty aiDifficulty) {
        if (aiDifficulty == null) {
            aiDifficulty = AiDifficulty.EASY;
        }
        String aiName = "AI Opponent (" + aiDifficulty.getDisplayName() + ")";
        UUID aiPlayerId = UUID.randomUUID();
        Player aiPlayer = new Player(aiPlayerId, aiName);

        AiDecisionEngine engine = switch (aiDifficulty) {
            case HARD -> {
                HardAiDecisionEngine hard = new HardAiDecisionEngine(gameData.id, aiPlayer, gameRegistry, gameService, gameQueryService, combatAttackService, actionAvailabilityService, castingCostService, castingPermissionService, targetValidationService, targetLegalityService);
                hard.setMctsTimeBudgetMs(mctsTimeBudgetMs);
                // 0 = auto-size from available cores; tests bypass this service and
                // stay on the engine's single-threaded default
                hard.setMctsParallelism(mctsParallelism > 0
                        ? mctsParallelism
                        : MCTSEngine.autoParallelism());
                yield hard;
            }
            case MEDIUM -> new MediumAiDecisionEngine(gameData.id, aiPlayer, gameRegistry, gameService, gameQueryService, combatAttackService, actionAvailabilityService, castingCostService, castingPermissionService, targetValidationService, targetLegalityService);
            case EASY -> new EasyAiDecisionEngine(gameData.id, aiPlayer, gameRegistry, gameService, gameQueryService, combatAttackService, actionAvailabilityService, castingCostService, castingPermissionService, targetValidationService, targetLegalityService);
        };
        String schedulerId = "ai-" + gameData.id + "-" + aiPlayerId;
        AiDecisionScheduler aiDecisionScheduler = new AiDecisionScheduler(
                schedulerId, engine, aiDifficulty.getDecisionDelayMs());
        decisionEventSubscriber.register(gameData.id, aiPlayerId, aiDecisionScheduler);

        try {
            mutationCoordinator.mutate(gameData, () -> {
                // Mark this player as AI-controlled so auto-pass always hands it a priority window
                // when it can act, instead of treating it like a human bound by auto-stop settings.
                gameData.aiPlayerIds.add(aiPlayerId);

                // Join the game — this triggers initializeGame() and joins this outer setup action.
                gameSetupService.joinGame(gameData, aiPlayer, aiDeckId);
            });
        } catch (RuntimeException e) {
            decisionEventSubscriber.unregister(gameData.id, aiPlayerId);
            aiDecisionScheduler.close();
            throw e;
        }

        log.info("AI opponent joined game {} with deck {}", gameData.id, aiDeckId);
    }
}
