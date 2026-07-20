package com.github.laxika.magicalvibes.model.effect;

/**
 * Look at the top card of the controller's library. If it's a land card, or a creature card whose
 * mana value is less than or equal to the number of loyalty counters on the source permanent, the
 * controller may put that card onto the battlefield. Otherwise (or if declined), it stays on top.
 *
 * <p>Used by Nissa, Steward of Elements (her 0 loyalty ability). The loyalty threshold is read from
 * the source permanent at resolution.
 */
public record LookAtTopCardPutLandOrCreatureWithinLoyaltyEffect() implements CardEffect {
}
