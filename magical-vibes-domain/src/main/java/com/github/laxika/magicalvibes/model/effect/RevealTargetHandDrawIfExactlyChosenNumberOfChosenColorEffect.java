package com.github.laxika.magicalvibes.model.effect;

/**
 * The target player reveals their hand. If exactly the source permanent's chosen number of cards
 * in that hand have the source permanent's chosen color, the controller draws a card.
 */
public record RevealTargetHandDrawIfExactlyChosenNumberOfChosenColorEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
