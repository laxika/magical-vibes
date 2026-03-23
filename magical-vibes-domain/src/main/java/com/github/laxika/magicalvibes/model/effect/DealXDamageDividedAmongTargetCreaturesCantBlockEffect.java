package com.github.laxika.magicalvibes.model.effect;

/**
 * "Deals X damage divided as you choose among any number of target creatures.
 * Creatures dealt damage this way can't block this turn."
 *
 * <p>X comes from the stack entry's {@code xValue} (typically a variable loyalty cost).
 * Damage assignments (target UUID → amount) are stored on
 * {@code StackEntry.damageAssignments}. After dealing damage, each creature that
 * was actually dealt damage has its {@code cantBlockThisTurn} flag set.
 *
 * <p>Used by Huatli, Warrior Poet's −X loyalty ability.
 */
public record DealXDamageDividedAmongTargetCreaturesCantBlockEffect() implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
