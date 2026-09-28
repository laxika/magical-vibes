package com.github.laxika.magicalvibes.model.effect;

/**
 * Returns one targeted graveyard card, then has a targeted player return a card from their
 * graveyard to their hand.
 *
 * <p>The two component effects are registered to their respective target groups by the card that
 * uses this ordered effect. Their existing handlers are then reused during resolution.</p>
 */
public record ReturnTargetCardThenTargetPlayerReturnsCardFromGraveyardToHandEffect(
        ReturnCardFromGraveyardEffect targetCardReturn,
        TargetPlayerReturnsCardFromGraveyardToHandEffect targetPlayerReturn)
        implements TargetedGraveyardAndPlayerEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.anyOf(
                TargetPredicates.graveyardCard(targetCardReturn.source()),
                TargetPredicates.player()));
    }
}
