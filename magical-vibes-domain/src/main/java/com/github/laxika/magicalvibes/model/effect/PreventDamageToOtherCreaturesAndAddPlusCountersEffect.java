package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect that prevents damage to creatures controlled by the source permanent's controller
 * and replaces it with +1/+1 counters. The default form excludes the source itself (Vigor).
 */
public record PreventDamageToOtherCreaturesAndAddPlusCountersEffect(
        boolean includeSource, boolean noncombatOnly, boolean sourceOnly) implements CardEffect {

    public PreventDamageToOtherCreaturesAndAddPlusCountersEffect() {
        this(false, false, false);
    }

    public PreventDamageToOtherCreaturesAndAddPlusCountersEffect(boolean includeSource) {
        this(includeSource, false, false);
    }

    /** Creates the source-only variant, optionally restricted to noncombat damage. */
    public static PreventDamageToOtherCreaturesAndAddPlusCountersEffect forSource(boolean noncombatOnly) {
        return new PreventDamageToOtherCreaturesAndAddPlusCountersEffect(false, noncombatOnly, true);
    }
}
