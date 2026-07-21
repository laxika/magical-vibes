package com.github.laxika.magicalvibes.model.effect;

/**
 * Counters every spell chosen for this effect's target group — an "up to N target spells" counter
 * (Double Negative: "Counter up to two target spells"). Bound to a single {@code target(filter, 0, N)}
 * group; the handler counters each spell in that group, mirroring {@link DealDamageToEachTargetEffect}'s
 * each-target resolution. For the ordinary single-target "counter target spell" use {@link CounterSpellEffect}.
 */
public record CounterEachTargetSpellEffect() implements CounterSpellingEffect {

    @Override public TargetSpec targetSpec() { return TargetSpec.benign(TargetCategory.SPELL_ON_STACK); }
}
