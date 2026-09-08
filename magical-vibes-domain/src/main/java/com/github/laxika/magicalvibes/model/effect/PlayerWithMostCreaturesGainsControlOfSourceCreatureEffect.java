package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** The unique player with the most matching creatures gains permanent control of the source creature. */
public record PlayerWithMostCreaturesGainsControlOfSourceCreatureEffect(PermanentPredicate creatureFilter)
        implements ControlStealingEffect {

    /** Counts all creatures, preserving the original effect's behavior. */
    public PlayerWithMostCreaturesGainsControlOfSourceCreatureEffect() {
        this(new PermanentIsCreaturePredicate());
    }

    @Override
    public ControlDuration controlDuration() {
        return ControlDuration.PERMANENT;
    }
}
