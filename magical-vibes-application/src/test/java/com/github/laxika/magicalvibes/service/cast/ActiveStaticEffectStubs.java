package com.github.laxika.magicalvibes.service.cast;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import org.mockito.ArgumentMatchers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

/**
 * Answers a mocked {@link GameQueryService}'s board scans of static abilities
 * ({@code activeStaticEffects}, {@code getActiveStaticEffects}) the way the real service does: each battlefield
 * permanent's printed static effects, none from a face-down permanent or one that has lost its printed abilities,
 * conditionals judged and sequences flattened. Conditionals are judged by a real {@link ConditionEvaluationService}
 * over the mocks, standing in for the query service's own evaluator.
 */
public final class ActiveStaticEffectStubs {

    private ActiveStaticEffectStubs() {
    }

    public static void stub(GameQueryService gameQueryService, PredicateEvaluationService predicateEvaluationService) {
        ConditionEvaluationService conditions =
                new ConditionEvaluationService(gameQueryService, predicateEvaluationService);
        lenient().when(gameQueryService.activeStaticEffects(any(GameData.class),
                        ArgumentMatchers.<Class<CardEffect>>any()))
                .thenAnswer(inv -> {
                    GameData gameData = inv.getArgument(0);
                    List<GameQueryService.ActiveStaticEffect<CardEffect>> found = new ArrayList<>();
                    for (UUID playerId : gameData.orderedPlayerIds) {
                        collect(gameQueryService, conditions, gameData, playerId, inv.getArgument(1), found);
                    }
                    return found;
                });
        lenient().when(gameQueryService.activeStaticEffects(any(GameData.class), any(),
                        ArgumentMatchers.<Class<CardEffect>>any()))
                .thenAnswer(inv -> {
                    List<GameQueryService.ActiveStaticEffect<CardEffect>> found = new ArrayList<>();
                    collect(gameQueryService, conditions, inv.getArgument(0), inv.getArgument(1),
                            inv.getArgument(2), found);
                    return found;
                });
        lenient().when(gameQueryService.getActiveStaticEffects(any(GameData.class), any(Permanent.class), any()))
                .thenAnswer(inv -> staticsOf(gameQueryService, conditions, inv.getArgument(0),
                        inv.getArgument(1), inv.getArgument(2)));
        lenient().when(gameQueryService.getActiveStaticEffects(any(GameData.class), any(Permanent.class)))
                .thenAnswer(inv -> {
                    GameData gameData = inv.getArgument(0);
                    Permanent permanent = inv.getArgument(1);
                    UUID controllerId = gameData.orderedPlayerIds.stream()
                            .filter(pid -> gameData.playerBattlefields.getOrDefault(pid, List.of()).contains(permanent))
                            .findFirst().orElse(null);
                    return staticsOf(gameQueryService, conditions, gameData, permanent, controllerId);
                });
    }

    private static void collect(GameQueryService gameQueryService, ConditionEvaluationService conditions,
                                GameData gameData, UUID controllerId, Class<?> type,
                                List<GameQueryService.ActiveStaticEffect<CardEffect>> found) {
        List<Permanent> battlefield = controllerId == null ? null : gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return;
        for (Permanent permanent : battlefield) {
            for (CardEffect effect : staticsOf(gameQueryService, conditions, gameData, permanent, controllerId)) {
                if (type.isInstance(effect)) {
                    found.add(new GameQueryService.ActiveStaticEffect<>(effect, permanent, controllerId));
                }
            }
        }
    }

    private static List<CardEffect> staticsOf(GameQueryService gameQueryService, ConditionEvaluationService conditions,
                                              GameData gameData, Permanent permanent, UUID controllerId) {
        if (permanent.isFaceDown() || gameQueryService.hasLostPrintedAbilities(gameData, permanent)) {
            return List.of();
        }
        List<CardEffect> active = new ArrayList<>();
        for (CardEffect effect : permanent.getCard().getStaticEffects()) {
            unwrap(conditions, gameData, permanent, controllerId, effect, active);
        }
        return active;
    }

    private static void unwrap(ConditionEvaluationService conditions, GameData gameData, Permanent source,
                               UUID controllerId, CardEffect effect, List<CardEffect> active) {
        if (effect instanceof ConditionalEffect conditional) {
            if (conditions.isMet(gameData, conditional.condition(),
                    ConditionContext.forStaticEffect(source, controllerId))) {
                unwrap(conditions, gameData, source, controllerId, conditional.wrapped(), active);
            }
            return;
        }
        if (effect instanceof SequenceEffect sequence) {
            for (CardEffect step : sequence.steps()) {
                unwrap(conditions, gameData, source, controllerId, step, active);
            }
            return;
        }
        active.add(effect);
    }
}
