package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;

/**
 * Returns the source card from its graveyard as an Aura attached to a legal permanent.
 * An optional retained ability lets a transforming card lose all other abilities as it becomes
 * the Aura.
 */
public record ReturnSourceAsAuraEffect(TargetFilter enchantFilter, ActivatedAbility retainedAbility,
                                       boolean losesOtherAbilities)
        implements CardEffect {

    public ReturnSourceAsAuraEffect(TargetFilter enchantFilter) {
        this(enchantFilter, null, false);
    }

    public ReturnSourceAsAuraEffect(TargetFilter enchantFilter, ActivatedAbility retainedAbility) {
        this(enchantFilter, retainedAbility, false);
    }

    public static ReturnSourceAsAuraEffect losingOtherAbilities(TargetFilter enchantFilter) {
        return new ReturnSourceAsAuraEffect(enchantFilter, null, true);
    }
}
