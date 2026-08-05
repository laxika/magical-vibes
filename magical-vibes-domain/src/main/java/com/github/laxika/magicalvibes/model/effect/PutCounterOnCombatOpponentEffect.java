package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Combat trigger: put {@code amount} counters of {@code counterType} on the combat opponent (the
 * creature this permanent blocks, or that becomes blocked by it) immediately on resolution.
 * Mindbender Spores' "Whenever this creature blocks a creature, put four fungus counters on that
 * creature."
 *
 * <p>The immediate sibling of {@link PutCounterOnCombatOpponentAtEndOfCombatEffect}, which instead
 * schedules the placement for end of combat. Being a {@link CombatOpponentReferencingEffect}, the
 * block pipeline bakes the combat opponent into the trigger's non-targeting {@code targetId}, so the
 * trigger can't fizzle and no card-level target filter is needed.
 *
 * @param counterType the type of counter to place on the combat opponent
 * @param amount      how many counters to place
 */
public record PutCounterOnCombatOpponentEffect(
        CounterType counterType,
        int amount
) implements CombatOpponentReferencingEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.permanent());
    }
}
