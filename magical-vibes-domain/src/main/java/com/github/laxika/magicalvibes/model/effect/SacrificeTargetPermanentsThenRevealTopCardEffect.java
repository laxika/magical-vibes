package com.github.laxika.magicalvibes.model.effect;

/**
 * Sacrifices every selected target permanent, then has each affected player's controller reveal
 * the top card of their library and put it onto the battlefield when it is a permanent card.
 * Nonpermanent revealed cards remain on top of their libraries.
 */
public record SacrificeTargetPermanentsThenRevealTopCardEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.permanent());
    }
}
