package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * "Distribute {@code total} {@code counterType} counters among one or two target creatures."
 *
 * <p>The counters are split <em>evenly</em> across this effect's chosen target group
 * ({@code floor(total / targetCount)} on each). This is exactly the forced distribution for the
 * "distribute two counters among one or two target creatures" family (Splendid Agony's two
 * -1/-1 counters, Common Bond's two +1/+1 counters): CR 601.2d requires each target to receive at
 * least one counter, so with a total of two and at most two targets the split is fully determined —
 * two on a single target, or one on each of two targets. Do <em>not</em> use this for a genuine
 * three-or-more distribution where the controller has a real choice of how to divide.</p>
 *
 * <p>The target group (count and legality) is declared on the card via {@code target(filter, min,
 * max)}; the handler reads {@code StackEntry.targetsForEffect(this)}.</p>
 *
 * @param counterType the counter to place.
 * @param total       the total number of counters to divide evenly among the targets.
 */
public record DistributeCountersAmongTargetsEffect(CounterType counterType, int total) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        boolean harmful = counterType == CounterType.MINUS_ONE_MINUS_ONE;
        return harmful
                ? TargetSpec.harmful(TargetCategory.CREATURE)
                : TargetSpec.benign(TargetCategory.CREATURE);
    }
}
