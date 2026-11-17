package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.UUID;

/**
 * The damage a {@link PreventedDamageCounterShield} has prevented so far, which is the number of +0/+1 counters its
 * delayed triggered ability puts on the creature as it resolves. It lasts until that ability resolves, which may be
 * after the shield has lapsed at the end of the turn (a spell cast during the end step), so damage prevented in
 * response to the ability is still counted.
 *
 * @param shieldId what the delayed triggered ability names the shield by
 * @param prevented the damage prevented so far
 */
public record PreventedDamageCounterTally(UUID shieldId, int prevented) implements CardEffect {
}
