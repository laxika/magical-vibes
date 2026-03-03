package com.github.laxika.magicalvibes.model.effect;

/**
 * Gains control of target permanent until end of turn.
 * Unlike {@link GainControlOfTargetCreatureUntilEndOfTurnEffect}, this effect
 * does not enforce creature-only targeting, allowing it to be used for any
 * permanent type (e.g. artifacts via Metallic Mastery). The card's own target
 * filter handles type restrictions.
 */
public record GainControlOfTargetPermanentUntilEndOfTurnEffect() implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
