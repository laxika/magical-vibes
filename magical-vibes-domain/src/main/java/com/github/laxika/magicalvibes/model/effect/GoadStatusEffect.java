package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** A timestamped effect that either applies or removes the goaded designation. */
public interface GoadStatusEffect extends CardEffect {

    PermanentPredicate affectedPredicate();

    boolean makesGoaded();
}
