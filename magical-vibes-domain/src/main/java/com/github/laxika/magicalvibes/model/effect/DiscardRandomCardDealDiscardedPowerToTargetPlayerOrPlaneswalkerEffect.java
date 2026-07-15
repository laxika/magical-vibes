package com.github.laxika.magicalvibes.model.effect;

/**
 * Discard a card at random. If the discarded card is a creature card, the source deals damage
 * equal to that card's power to target player or planeswalker.
 *
 * <p>The target is always chosen as the ability is put on the stack (before the random discard);
 * if the discarded card is not a creature card, the ability simply has no further effect.
 * Any player may be chosen; planeswalker permanents are also valid targets.
 *
 * <p>Used by Cragganwick Cremator.
 */
public record DiscardRandomCardDealDiscardedPowerToTargetPlayerOrPlaneswalkerEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.PLAYER_OR_PERMANENT);
    }
}
