package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * The delayed triggered ability of Sacred Boon and Scars of the Veteran: puts a +0/+1 counter on the creature it is
 * about (its entry's non-targeting object) for each 1 damage the shield identified by {@code shieldId} has prevented
 * by the time the ability resolves (damage prevented in response to it still counts).
 */
public record PutCountersForPreventedDamageEffect(UUID shieldId) implements CardEffect {
}
