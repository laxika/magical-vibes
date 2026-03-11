package com.github.laxika.magicalvibes.model.effect;

/**
 * Untaps all target permanents (multi-target variant of UntapTargetPermanentEffect).
 * Iterates over entry.getTargetPermanentIds() to untap each one.
 */
public record UntapAllTargetPermanentsEffect() implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
