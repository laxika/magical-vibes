package com.github.laxika.magicalvibes.model.effect;

/**
 * The player on the stack entry's {@code targetId} chooses a permanent they control at random and
 * sacrifices it. If a nonland permanent is sacrificed this way, the process repeats.
 *
 * <p>The random choice is made by the engine (no player input), so the whole loop resolves
 * synchronously. It ends as soon as a land is sacrificed or the player controls no permanents.
 *
 * <p>Used by Tyrant of Discord (ETB); pair with a {@code PlayerPredicateTargetFilter(OPPONENT)}.
 */
public record TargetPlayerSacrificesRandomPermanentsUntilLandEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PLAYER);
    }
}
