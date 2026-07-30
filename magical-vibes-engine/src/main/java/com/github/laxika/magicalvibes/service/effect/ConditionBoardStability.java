package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.condition.AllConditions;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.AnyOf;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.condition.Metalcraft;
import com.github.laxika.magicalvibes.model.condition.NotCondition;

import java.util.List;

/**
 * Whether a {@link Condition} may be evaluated inside the CR 613 layered pass without making the
 * memoized board unsound.
 *
 * <p>{@code LayerSystemService.computeBoardFingerprint} hashes the inputs the pass reads —
 * battlefield permanents and their mutable fields, graveyards, exile, hand sizes, top-of-library
 * identity, floating effects. A condition reading anything else (life totals, the active player,
 * poison counters, the per-turn event trackers, combat state) would let a cached board hand back a
 * verdict that is no longer true. Such conditions stay outside the pass, where the static-bonus
 * assembly re-evaluates them on every query.
 *
 * <p><strong>Default-deny.</strong> An unlisted condition keeps the pre-existing legacy-additive
 * behavior, so introducing a condition can never silently make the board cache stale; the cost of
 * forgetting to list one is a missed optimization, not a wrong answer. Only conditions that can
 * currently reach the admission path are listed — widening the set is tracked in
 * agent-docs/STATIC_EVALUATION_MIGRATION.md (stage C).
 */
public final class ConditionBoardStability {

    private ConditionBoardStability() {
    }

    /** True when evaluating the condition reads only state the board fingerprint covers. */
    public static boolean readsOnlyFingerprintedState(Condition condition) {
        return switch (condition) {
            // Three or more controlled artifacts: battlefield membership plus each permanent's
            // type, and the static branch counts through StaticEffectSupport
            // .isArtifactForStaticFilter, which reads the in-flight layer-4 state (CR 613.1d).
            case Metalcraft ignored -> true;
            case NotCondition c -> readsOnlyFingerprintedState(c.inner());
            case AllConditions c -> allStable(c.conditions());
            case AllOf c -> allStable(c.conditions());
            case AnyOf c -> allStable(c.conditions());
            default -> false;
        };
    }

    private static boolean allStable(List<Condition> conditions) {
        return conditions.stream().allMatch(ConditionBoardStability::readsOnlyFingerprintedState);
    }
}
