package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveals cards from the top of the controller's library until a nonland card is revealed.
 * The source deals damage equal to that card's mana value to the entry's any target, then the
 * nonland card is put into the controller's hand and the revealed lands are put on the bottom of
 * the library in any order.
 */
public record RevealUntilNonlandToHandRestToBottomDealManaValueDamageEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.anyTarget());
    }
}
