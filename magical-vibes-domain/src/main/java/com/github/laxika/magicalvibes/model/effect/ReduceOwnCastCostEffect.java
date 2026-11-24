package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Reduces this spell's own casting cost by the evaluated {@link DynamicAmount} of generic mana.
 * The single spell-self cost-reduction effect: fixed reductions use {@code new Fixed(n)},
 * "for each …" reductions use a counting amount (e.g. {@code CardsInGraveyard},
 * {@code PermanentCount}). Conditional reductions ("costs {N} less to cast if …") wrap this in a
 * {@link ConditionalEffect}. With {@code increase} set it is the matching surcharge, "this spell costs {N} more to
 * cast" ({@link #increase(DynamicAmount)}; Hurkyl's Final Meditation, Risen Executioner).
 */
public record ReduceOwnCastCostEffect(DynamicAmount amount, boolean increase) implements CardEffect {

    public ReduceOwnCastCostEffect(DynamicAmount amount) {
        this(amount, false);
    }

    /** "This spell costs {N} more to cast": the evaluated amount is added instead. */
    public static ReduceOwnCastCostEffect increase(DynamicAmount amount) {
        return new ReduceOwnCastCostEffect(amount, true);
    }

    /** An ability that modifies what its own object costs to cast functions on the stack (CR 113.6d). */
    @Override
    public AbilityZones staticAbilityZones() {
        return AbilityZones.STACK;
    }
}
