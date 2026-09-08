package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;

/**
 * Returns target spell or permanent with at least the given mana value to its owner's hand.
 */
public record ReturnTargetSpellOrPermanentToHandEffect(int minimumManaValue) implements RemovalEffect {

    public ReturnTargetSpellOrPermanentToHandEffect() {
        this(0);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.anyOf(
                TargetPredicates.permanents(new PermanentMinManaValuePredicate(minimumManaValue)),
                TargetPredicates.spells(new StackEntryNotPredicate(
                        new StackEntryMaxManaValuePredicate(minimumManaValue - 1)))));
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.BOUNCE;
    }
}
