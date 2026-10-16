package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;

import java.util.UUID;

/**
 * Returns the source card from its graveyard as an Aura attached to a legal permanent.
 * An optional retained ability lets a transforming card lose all other abilities as it becomes
 * the Aura.
 *
 * <p>{@code auraStaticEffect}, when set, is the static ability the returned Aura has (Harold and
 * Bob's "Enchanted Forest has ..."). It is carried by this effect rather than read from the returning
 * card, so a card that was merely a copy of the source (a Clone) still returns with it.
 * {@code dyingCardId} is the card that died, bound when the trigger is collected: for a copy it is the
 * copying card, not the copy's own identity.
 */
public record ReturnSourceAsAuraEffect(TargetFilter enchantFilter, ActivatedAbility retainedAbility,
                                       boolean losesOtherAbilities, CardEffect auraStaticEffect,
                                       UUID dyingCardId)
        implements CardEffect, DyingCreatureCardAwareEffect {

    public ReturnSourceAsAuraEffect(TargetFilter enchantFilter) {
        this(enchantFilter, null, false, null, null);
    }

    public ReturnSourceAsAuraEffect(TargetFilter enchantFilter, ActivatedAbility retainedAbility) {
        this(enchantFilter, retainedAbility, false, null, null);
    }

    public ReturnSourceAsAuraEffect(TargetFilter enchantFilter, ActivatedAbility retainedAbility,
                                    boolean losesOtherAbilities) {
        this(enchantFilter, retainedAbility, losesOtherAbilities, null, null);
    }

    public static ReturnSourceAsAuraEffect losingOtherAbilities(TargetFilter enchantFilter) {
        return new ReturnSourceAsAuraEffect(enchantFilter, null, true, null, null);
    }

    /** The Aura loses all other abilities and has only {@code auraStaticEffect}. */
    public static ReturnSourceAsAuraEffect losingOtherAbilities(TargetFilter enchantFilter,
                                                                CardEffect auraStaticEffect) {
        return new ReturnSourceAsAuraEffect(enchantFilter, null, true, auraStaticEffect, null);
    }

    @Override
    public CardEffect boundToDyingCard(UUID dyingCardId) {
        return new ReturnSourceAsAuraEffect(enchantFilter, retainedAbility, losesOtherAbilities,
                auraStaticEffect, dyingCardId);
    }
}
