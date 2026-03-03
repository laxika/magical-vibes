package com.github.laxika.magicalvibes.model.effect;

/**
 * Attaches the targeted permanent to the source permanent.
 * Used for effects that steal equipment and attach it to the attacking creature
 * (e.g. Ogre Geargrabber). Typically combined with
 * {@link GainControlOfTargetPermanentUntilEndOfTurnEffect}.
 */
public record AttachTargetToSourcePermanentEffect() implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
