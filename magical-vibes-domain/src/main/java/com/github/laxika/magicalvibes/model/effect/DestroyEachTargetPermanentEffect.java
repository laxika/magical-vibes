package com.github.laxika.magicalvibes.model.effect;

/**
 * Destroys each permanent targeted by the spell/ability (all of {@code entry.getTargetIds()}).
 * Bound to a single multi-target group, so it destroys every chosen target — used by
 * "Destroy X target nonblack creatures" (Dregs of Sorrow) via {@code targetX(...)}.
 *
 * @param cannotBeRegenerated whether the destroyed permanents cannot be regenerated
 */
public record DestroyEachTargetPermanentEffect(boolean cannotBeRegenerated) implements CardEffect {

    public DestroyEachTargetPermanentEffect() {
        this(false);
    }

    @Override
    public boolean canTargetPermanent() {
        return true;
    }

    @Override
    public boolean isDamageOrDestruction() {
        return true;
    }
}
