package com.github.laxika.magicalvibes.model.effect;

/**
 * Target opponent reveals a card at random from their hand, then the controller gains life equal
 * to that card's mana value. If the hand is empty there is nothing to reveal and no life is gained.
 */
public record RevealRandomCardFromTargetPlayerHandGainLifeEqualToManaValueEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
