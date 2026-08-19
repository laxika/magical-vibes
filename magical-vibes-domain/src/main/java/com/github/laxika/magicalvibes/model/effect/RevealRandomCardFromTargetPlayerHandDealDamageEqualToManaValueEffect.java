package com.github.laxika.magicalvibes.model.effect;

/**
 * Target player reveals a card at random from their hand, then this effect deals damage to that
 * player equal to the revealed card's mana value. If the hand is empty, nothing is revealed and no
 * damage is dealt.
 */
public record RevealRandomCardFromTargetPlayerHandDealDamageEqualToManaValueEffect()
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }
}
