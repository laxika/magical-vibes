package com.github.laxika.magicalvibes.model.effect;

/**
 * "Deals N damage divided as you choose among up to M target creatures."
 *
 * <p>Damage assignments (target UUID → amount) are stored on
 * {@code StackEntry.damageAssignments}. Target filtering (e.g. color
 * restrictions) is handled via the card's {@code TargetFilter}.
 *
 * <p>Used by cards like Ignite Disorder (3 damage divided among 1-3
 * target white and/or blue creatures).
 */
public record DealDividedDamageAmongTargetCreaturesEffect(int totalDamage) implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
