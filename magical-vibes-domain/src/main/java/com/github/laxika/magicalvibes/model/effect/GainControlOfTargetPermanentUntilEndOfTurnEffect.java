package com.github.laxika.magicalvibes.model.effect;

/**
 * Gains control of target permanent until end of turn.
 * The card's own target filter handles type restrictions (e.g. creature-only
 * for Threaten, artifact-only for Metallic Mastery).
 */
public record GainControlOfTargetPermanentUntilEndOfTurnEffect() implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
