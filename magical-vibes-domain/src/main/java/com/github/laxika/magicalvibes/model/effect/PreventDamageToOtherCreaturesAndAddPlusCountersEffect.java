package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: "If damage would be dealt to another creature you control, prevent that damage.
 * Put a +1/+1 counter on that creature for each 1 damage prevented this way." (e.g. Vigor), or the
 * same replacement applied to the source itself (e.g. Anti-Venom, Horrifying Healer).
 * <p>
 * The no-argument constructor excludes the source itself; the boolean constructor can include it.
 * Both forms cover combat and noncombat damage. Hooked in
 * {@link com.github.laxika.magicalvibes.service.DamagePreventionService#applyCreaturePreventionShield}.
 */
public record PreventDamageToOtherCreaturesAndAddPlusCountersEffect(boolean includeSource) implements CardEffect {

    public PreventDamageToOtherCreaturesAndAddPlusCountersEffect() {
        this(false);
    }
}
