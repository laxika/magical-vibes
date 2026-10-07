package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Reveals cards from the top of the controller's library until a nonland card is revealed or the
 * library is empty, then deals damage equal to that card's mana value to the entry's any target.
 * All revealed cards are put on the bottom of the library in any order.
 *
 * <p>When {@code reflexive} is set the effect doesn't target: revealing a nonland card queues a
 * reflexive trigger that deals the damage to any target, chosen as it goes on the stack
 * (Calibrated Blast: "When you reveal a nonland card this way, ...").
 */
public record RevealUntilNonlandBottomThenDealManaValueDamageEffect(
        TargetPredicate targetPredicate,
        UUID fixedTargetId,
        boolean randomizeBottom,
        boolean reflexive
) implements CardEffect {

    public RevealUntilNonlandBottomThenDealManaValueDamageEffect(TargetPredicate targetPredicate,
                                                                 UUID fixedTargetId,
                                                                 boolean randomizeBottom) {
        this(targetPredicate, fixedTargetId, randomizeBottom, false);
    }

    public RevealUntilNonlandBottomThenDealManaValueDamageEffect() {
        this(TargetPredicates.anyTarget(), null, false);
    }

    public RevealUntilNonlandBottomThenDealManaValueDamageEffect(TargetPredicate targetPredicate) {
        this(targetPredicate, null, false);
    }

    public RevealUntilNonlandBottomThenDealManaValueDamageEffect(TargetPredicate targetPredicate,
                                                                 boolean randomizeBottom) {
        this(targetPredicate, null, randomizeBottom);
    }

    /** Non-targeting reveal whose nonland reveal triggers a reflexive any-target damage ability. */
    public static RevealUntilNonlandBottomThenDealManaValueDamageEffect reflexiveAnyTarget(boolean randomizeBottom) {
        return new RevealUntilNonlandBottomThenDealManaValueDamageEffect(
                TargetPredicates.anyTarget(), null, randomizeBottom, true);
    }

    @Override
    public TargetSpec targetSpec() {
        return reflexive ? TargetSpec.NONE : TargetSpec.harmful(targetPredicate);
    }
}
