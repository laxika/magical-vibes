package com.github.laxika.magicalvibes.model.effect;

/**
 * Secrets of Strixhaven "Prepared": the target creature becomes unprepared.
 * <p>
 * On resolution, if the target is prepared, it loses the prepared designation and its associated
 * prepare-spell copy in exile ceases to exist. A no-op if the target is not prepared.
 */
public record MakeTargetCreatureUnpreparedEffect() implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
