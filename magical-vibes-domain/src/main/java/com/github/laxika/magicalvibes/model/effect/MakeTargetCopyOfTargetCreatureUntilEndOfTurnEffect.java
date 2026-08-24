package com.github.laxika.magicalvibes.model.effect;

/**
 * Makes the creature chosen for one target group become a copy of the creature chosen for
 * another target group until end of turn.
 */
public record MakeTargetCopyOfTargetCreatureUntilEndOfTurnEffect(int targetGroup, int copySourceGroup)
        implements CardEffect {

    public MakeTargetCopyOfTargetCreatureUntilEndOfTurnEffect() {
        this(0, 1);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent());
    }
}
