package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Static effect: creatures you control of the permanent's chosen subtype get +P/+T.
 * Used by Vanquisher's Banner and similar cards that choose a creature type as they enter
 * the battlefield. The chosen subtype is stored on the source permanent at runtime via
 * {@code Permanent.getChosenSubtype()}.
 * <p>
 * When {@code scalingCounter} is non-null the boost is multiplied by the number of counters
 * of that type on the source permanent ("+1/+1 for each charge counter on this artifact" —
 * Door of Destinies). A {@code null} value applies the flat {@code powerBoost}/{@code toughnessBoost}.
 */
public record BoostCreaturesOfChosenSubtypeEffect(int powerBoost, int toughnessBoost,
                                                  CounterType scalingCounter) implements CardEffect {

    public BoostCreaturesOfChosenSubtypeEffect(int powerBoost, int toughnessBoost) {
        this(powerBoost, toughnessBoost, null);
    }
}
