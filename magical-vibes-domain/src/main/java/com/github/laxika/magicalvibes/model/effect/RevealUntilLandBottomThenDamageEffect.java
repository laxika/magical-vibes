package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveals cards from the top of the controller's library until a land is revealed or the
 * library is empty, then deals damage equal to the number of nonland cards revealed to the
 * entry's any target. If the revealed land is a Mountain, the damage is doubled. The revealed
 * cards are put on the bottom of the library in any order.
 */
public record RevealUntilLandBottomThenDamageEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.anyTarget());
    }
}
