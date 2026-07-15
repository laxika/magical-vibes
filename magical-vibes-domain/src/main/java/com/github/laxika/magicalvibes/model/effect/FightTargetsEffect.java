package com.github.laxika.magicalvibes.model.effect;

/**
 * The creature chosen for one target group fights the creature chosen for another target
 * group: each deals damage equal to its power to the other (CR 701.12). Both use effective
 * power at resolution, and protection is checked against each creature's own color rather
 * than the spell's color. If either chosen creature is gone at resolution, no damage is dealt.
 *
 * <p>This is the only effect family that inherently reads two targets, so the groups live in
 * DATA: {@code firstTargetGroup}/{@code secondTargetGroup} are indices into the card's
 * {@code target(...)} declarations (see {@code StackEntry.targetsForGroup}). For activated
 * abilities, which declare targets via a flat multi-target filter list instead, the indices
 * address flat target positions. The default groups are 0 and 1.</p>
 */
public record FightTargetsEffect(int firstTargetGroup, int secondTargetGroup) implements CardEffect {

    /** "Target creature fights another target creature" — groups 0 and 1. */
    public FightTargetsEffect() {
        this(0, 1);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PLAYER_OR_PERMANENT);
    }
}
