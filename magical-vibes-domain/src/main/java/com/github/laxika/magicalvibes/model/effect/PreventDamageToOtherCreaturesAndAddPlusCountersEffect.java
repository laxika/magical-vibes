package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect that prevents damage to creatures controlled by the source permanent's controller
 * and replaces it with +1/+1 counters. The default form excludes the source itself (Vigor).
 *
 * <p>{@code countersForUnpreventableDamage} is set for "prevent that damage and put that many +1/+1
 * counters" wording (Anti-Venom): when the damage can't be prevented the counters are still put on
 * (CR 615.12). "For each 1 damage prevented this way" wording (Vigor, Stormwild Capridor) leaves it
 * unset, so unpreventable damage adds no counters.
 */
public record PreventDamageToOtherCreaturesAndAddPlusCountersEffect(
        boolean includeSource, boolean noncombatOnly, boolean sourceOnly,
        boolean countersForUnpreventableDamage) implements CardEffect {

    public PreventDamageToOtherCreaturesAndAddPlusCountersEffect() {
        this(false, false, false, false);
    }

    public PreventDamageToOtherCreaturesAndAddPlusCountersEffect(boolean includeSource) {
        this(includeSource, false, false, false);
    }

    /** Creates the source-only variant, optionally restricted to noncombat damage. */
    public static PreventDamageToOtherCreaturesAndAddPlusCountersEffect forSource(boolean noncombatOnly) {
        return new PreventDamageToOtherCreaturesAndAddPlusCountersEffect(false, noncombatOnly, true, false);
    }

    /** Source-only variant for "prevent that damage and put that many +1/+1 counters on it" (Anti-Venom). */
    public static PreventDamageToOtherCreaturesAndAddPlusCountersEffect forSourceThatManyCounters() {
        return new PreventDamageToOtherCreaturesAndAddPlusCountersEffect(false, false, true, true);
    }
}
