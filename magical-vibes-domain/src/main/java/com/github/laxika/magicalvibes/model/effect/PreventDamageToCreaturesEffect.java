package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: "Prevent all [noncombat] damage that would be dealt to creatures [you control]."
 * (e.g. Inner Sanctum, Mark of Asylum, Bubble Matrix)
 * <p>
 * Evaluated when damage would be dealt, so it covers creatures that arrive after this resolves.
 * Hooked in
 * {@link com.github.laxika.magicalvibes.service.DamagePreventionService#applyCreaturePreventionShield}.
 *
 * @param noncombatOnly {@code true} prevents only noncombat damage (Mark of Asylum); {@code false}
 *                      prevents all damage, combat included (Inner Sanctum)
 * @param allCreatures  {@code true} covers every creature on the battlefield regardless of controller
 *                      (Bubble Matrix); {@code false} only creatures the source's controller controls
 */
public record PreventDamageToCreaturesEffect(boolean noncombatOnly, boolean allCreatures) implements CardEffect {

    /**
     * "Prevent all [noncombat] damage that would be dealt to creatures you control."
     */
    public static PreventDamageToCreaturesEffect youControl(boolean noncombatOnly) {
        return new PreventDamageToCreaturesEffect(noncombatOnly, false);
    }

    /**
     * "Prevent all damage that would be dealt to creatures" — every creature, both players.
     */
    public static PreventDamageToCreaturesEffect all() {
        return new PreventDamageToCreaturesEffect(false, true);
    }
}
