package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.condition.Condition;

/**
 * "This spell can't be countered."
 * Static ability on the card — checked during counter-spell resolution.
 * Added as a static ability.
 *
 * <p>A {@code null} {@code condition} means the spell is always uncounterable (Carnage Tyrant,
 * Combust, …). A non-null condition makes it uncounterable only while that condition holds,
 * evaluated against the spell's stack entry — e.g. Banefire's "If X is 5 or more" uses
 * {@code new SpellXAtLeast(5)}.
 */
public record CantBeCounteredEffect(Condition condition) implements CardEffect {

    public CantBeCounteredEffect() {
        this(null);
    }

    /** An ability that states its object can't be countered functions on the stack (CR 113.6g). */
    @Override
    public AbilityZones staticAbilityZones() {
        return AbilityZones.STACK;
    }
}
