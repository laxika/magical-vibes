package com.github.laxika.magicalvibes.model.effect;

/**
 * Destroys each permanent targeted by the spell/ability (all of {@code entry.getTargetIds()}).
 * Bound to a single multi-target group, so it destroys every chosen target — used by
 * "Destroy X target nonblack creatures" (Dregs of Sorrow) via {@code targetX(...)}.
 *
 * <p>Snapshots the number of permanents actually put into a graveyard this way onto the stack
 * entry as its event value, so a later effect on the same entry can reference "that many" via an
 * {@code EventValue} amount (Volcanic Eruption's mass damage).
 *
 * @param cannotBeRegenerated whether the destroyed permanents cannot be regenerated
 */
public record DestroyEachTargetPermanentEffect(boolean cannotBeRegenerated) implements CardEffect {

    public DestroyEachTargetPermanentEffect() {
        this(false);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.PLAYER_OR_PERMANENT);
    }
}
