package com.github.laxika.magicalvibes.model.effect;

/**
 * Makes the targeted player, or the controller of a companion targeted permanent, discard a
 * card with the greatest mana value in their hand. That player chooses among tied cards.
 */
public record DiscardGreatestManaValueCardEffect(boolean targetPermanentController) implements CardEffect {

    public DiscardGreatestManaValueCardEffect() {
        this(false);
    }

    public static DiscardGreatestManaValueCardEffect forTargetPermanentController() {
        return new DiscardGreatestManaValueCardEffect(true);
    }

    @Override
    public TargetSpec targetSpec() {
        return targetPermanentController ? TargetSpec.NONE : TargetSpec.benign(TargetPredicates.player());
    }
}
