package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

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
 * @param excludeSource {@code true} excludes the permanent carrying this effect from the protected
 *                      creatures (Crystal Barricade)
 * @param filter       optional predicate restricting the protected creatures
 */
public record PreventDamageToCreaturesEffect(boolean noncombatOnly, boolean allCreatures,
                                             boolean excludeSource, PermanentPredicate filter) implements CardEffect {

    public PreventDamageToCreaturesEffect(boolean noncombatOnly, boolean allCreatures) {
        this(noncombatOnly, allCreatures, false, null);
    }

    public PreventDamageToCreaturesEffect(boolean noncombatOnly, boolean allCreatures,
                                          boolean excludeSource) {
        this(noncombatOnly, allCreatures, excludeSource, null);
    }

    /**
     * "Prevent all [noncombat] damage that would be dealt to creatures you control."
     */
    public static PreventDamageToCreaturesEffect youControl(boolean noncombatOnly) {
        return youControl(noncombatOnly, null);
    }

    /**
     * "Prevent all [noncombat] damage that would be dealt to matching creatures you control."
     */
    public static PreventDamageToCreaturesEffect youControl(boolean noncombatOnly,
                                                             PermanentPredicate filter) {
        return new PreventDamageToCreaturesEffect(noncombatOnly, false, false, filter);
    }

    /**
     * "Prevent all [noncombat] damage that would be dealt to other creatures you control."
     */
    public static PreventDamageToCreaturesEffect otherCreaturesYouControl(boolean noncombatOnly) {
        return new PreventDamageToCreaturesEffect(noncombatOnly, false, true, null);
    }

    /**
     * "Prevent all damage that would be dealt to creatures" — every creature, both players.
     */
    public static PreventDamageToCreaturesEffect all() {
        return new PreventDamageToCreaturesEffect(false, true, false, null);
    }
}
