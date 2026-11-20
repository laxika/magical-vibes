package com.github.laxika.magicalvibes.model.effect;

/**
 * Target creature can block {@code additionalBlocks} more creatures this turn (beyond its base one).
 * A one-shot, until-end-of-turn version of the static {@link GrantAdditionalBlockEffect}: the grant
 * is a floating effect on the target until end of turn, summed by {@code GameData.additionalBlocksThisTurn}.
 * Used by Act of Heroism.
 *
 * <p>With {@link Subjects#source()} the grant applies to the source permanent instead of a target,
 * for self-pumping activated abilities such as Mounted Archers' {@code {W}}.
 */
public record GrantAdditionalBlockToTargetUntilEndOfTurnEffect(int additionalBlocks, PermanentSubject subject)
        implements CardEffect {

    public GrantAdditionalBlockToTargetUntilEndOfTurnEffect(int additionalBlocks) {
        this(additionalBlocks, Subjects.target());
    }

    @Override
    public TargetSpec targetSpec() {
        return subject instanceof PermanentSubject.Target
                ? TargetSpec.benign(TargetPredicates.creature())
                : TargetSpec.NONE;
    }
}
