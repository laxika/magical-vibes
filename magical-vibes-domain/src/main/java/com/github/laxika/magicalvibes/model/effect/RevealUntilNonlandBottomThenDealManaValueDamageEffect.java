package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Reveals cards from the top of the controller's library until a nonland card is revealed or the
 * library is empty, then deals damage equal to that card's mana value to the entry's any target.
 * All revealed cards are put on the bottom of the library in any order.
 */
public record RevealUntilNonlandBottomThenDealManaValueDamageEffect(
        TargetPredicate targetPredicate,
        UUID fixedTargetId
) implements CardEffect {

    public RevealUntilNonlandBottomThenDealManaValueDamageEffect() {
        this(TargetPredicates.anyTarget(), null);
    }

    public RevealUntilNonlandBottomThenDealManaValueDamageEffect(TargetPredicate targetPredicate) {
        this(targetPredicate, null);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(targetPredicate);
    }
}
