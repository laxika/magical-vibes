package com.github.laxika.magicalvibes.model.effect;

/**
 * Makes the targeted player discard a nonland card with the greatest mana value among cards in
 * their hand. The targeted player chooses among tied cards.
 */
public record DiscardGreatestManaValueNonlandCardEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
