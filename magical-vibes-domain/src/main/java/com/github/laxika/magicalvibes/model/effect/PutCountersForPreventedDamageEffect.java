package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Resolution half of Sacred Boon's delayed trigger: puts a +0/+1 counter on the trigger's source
 * permanent for each 1 damage the shield identified by {@code shieldId} has prevented by the time
 * the trigger resolves (damage prevented in response to the trigger still counts).
 */
public record PutCountersForPreventedDamageEffect(UUID shieldId) implements CardEffect {
}
