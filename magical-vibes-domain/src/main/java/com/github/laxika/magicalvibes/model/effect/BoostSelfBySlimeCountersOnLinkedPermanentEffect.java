package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Characteristic-defining ability placed on tokens: "This creature's power and toughness are
 * each equal to the number of slime counters on [linked permanent]."
 * The token has base 0/0; this effect provides +N/+N where N = slime counters on the linked
 * permanent. If the linked permanent leaves the battlefield, N = 0 and the token dies to SBA.
 */
public record BoostSelfBySlimeCountersOnLinkedPermanentEffect(UUID linkedPermanentId) implements CardEffect {

    @Override
    public boolean isPowerToughnessDefining() { return true; }
}
