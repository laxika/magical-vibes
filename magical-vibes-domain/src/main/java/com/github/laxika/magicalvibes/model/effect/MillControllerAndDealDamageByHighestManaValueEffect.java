package com.github.laxika.magicalvibes.model.effect;

/**
 * Mills a number of cards from the controller's library, then deals damage to any target
 * equal to the greatest mana value among the milled cards.
 * Used by Heretic's Punishment.
 */
public record MillControllerAndDealDamageByHighestManaValueEffect(int count) implements CardEffect {

    @Override
    public boolean canTargetPlayer() {
        return true;
    }

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
