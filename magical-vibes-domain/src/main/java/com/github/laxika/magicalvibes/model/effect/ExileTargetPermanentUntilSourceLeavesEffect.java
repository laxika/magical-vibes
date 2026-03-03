package com.github.laxika.magicalvibes.model.effect;

/**
 * Exile target permanent until the source permanent leaves the battlefield.
 * When the source leaves (by any means — death, bounce, exile), the exiled card
 * returns to the battlefield under its owner's control.
 * Used by O-ring style creatures like Leonin Relic-Warder.
 */
public record ExileTargetPermanentUntilSourceLeavesEffect() implements CardEffect {
    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
