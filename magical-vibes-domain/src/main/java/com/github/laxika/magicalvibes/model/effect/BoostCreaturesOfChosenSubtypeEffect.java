package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Static effect: creatures of the permanent's chosen subtype get +P/+T.
 * Used by Vanquisher's Banner and similar cards that choose a creature type as they enter
 * the battlefield. The chosen subtype is stored on the source permanent at runtime via
 * {@code Permanent.getChosenSubtype()}.
 * <p>
 * When {@code scalingCounter} is non-null the boost is multiplied by the number of counters
 * of that type on the source permanent ("+1/+1 for each charge counter on this artifact" —
 * Door of Destinies). A {@code null} value applies the flat {@code powerBoost}/{@code toughnessBoost}.
 * <p>
 * When {@code allControllers} is {@code false} (default) only creatures the source's controller
 * controls are boosted (Vanquisher's Banner, Door of Destinies). When {@code true} every creature
 * of the chosen type is boosted regardless of controller, including the source itself if it is a
 * creature of that type (Brass Herald — "Creatures of the chosen type get +1/+1").
 */
public record BoostCreaturesOfChosenSubtypeEffect(int powerBoost, int toughnessBoost,
                                                  CounterType scalingCounter,
                                                  boolean allControllers) implements CardEffect {

    public BoostCreaturesOfChosenSubtypeEffect(int powerBoost, int toughnessBoost) {
        this(powerBoost, toughnessBoost, null, false);
    }

    public BoostCreaturesOfChosenSubtypeEffect(int powerBoost, int toughnessBoost, CounterType scalingCounter) {
        this(powerBoost, toughnessBoost, scalingCounter, false);
    }
}
