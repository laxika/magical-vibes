package com.github.laxika.magicalvibes.model.effect;

/**
 * The creature chosen for one target group must block the creature chosen for another target
 * group this turn if able (CR 509.1c). Used by "Target creature blocks target creature this
 * turn if able." spells like Hunt Down.
 *
 * <p>Like {@link FightTargetsEffect}, this reads two targets, so the groups live in DATA:
 * {@code blockerTargetGroup}/{@code blockedTargetGroup} are indices into the card's
 * {@code target(...)} declarations (see {@code StackEntry.targetsForGroup}). The default groups
 * are 0 (the creature forced to block) and 1 (the creature that must be blocked).</p>
 */
public record MustBlockTargetCreatureEffect(int blockerTargetGroup, int blockedTargetGroup) implements CardEffect {

    /** "Target creature blocks target creature this turn if able" — groups 0 and 1. */
    public MustBlockTargetCreatureEffect() {
        this(0, 1);
    }

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
