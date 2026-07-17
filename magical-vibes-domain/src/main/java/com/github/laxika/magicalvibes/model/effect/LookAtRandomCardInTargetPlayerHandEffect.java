package com.github.laxika.magicalvibes.model.effect;

/**
 * The controller privately looks at a card chosen at random from target player's hand.
 * Unlike {@link RevealRandomCardFromTargetPlayerHandEffect} (a public reveal), the chosen
 * card is shown only to the effect's controller. Used by Urza's Bauble.
 */
public record LookAtRandomCardInTargetPlayerHandEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PLAYER);
    }
}
