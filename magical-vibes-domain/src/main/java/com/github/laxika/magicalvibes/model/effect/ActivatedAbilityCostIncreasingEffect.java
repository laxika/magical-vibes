package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.UUID;

/**
 * Capability interface for static effects that tax the activation cost of activated abilities of
 * matching permanents (e.g. Gloom: "Activated abilities of white enchantments cost {3} more to
 * activate."). The default tax is symmetric; specialized modifiers can narrow it to particular
 * activations, and it is collected by {@code CastingCostService} without naming the concrete
 * effect type.
 */
public interface ActivatedAbilityCostIncreasingEffect extends CardEffect {

    /** The permanents whose activated abilities are taxed. */
    PermanentPredicate affectedPermanents();

    /** Extra generic mana required to activate a matching permanent's ability. */
    int additionalGenericCost();

    /** Whether this tax applies to the particular activation being evaluated. */
    default boolean appliesTo(ActivatedAbility ability, boolean manaAbility,
                              UUID activatingPlayerId, UUID sourceControllerId) {
        return true;
    }
}
