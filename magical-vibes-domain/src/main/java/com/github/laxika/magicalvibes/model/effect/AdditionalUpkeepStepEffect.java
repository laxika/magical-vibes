package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

import java.util.Objects;

/**
 * "You get [N] additional upkeep step(s) after this step / after this phase". The count is evaluated as the effect
 * resolves; a count of 0 or less adds none.
 *
 * <p>{@link Placement#AFTER_THIS_STEP} (Paradox Haze, The Ninth Doctor) adds the steps after the current upkeep step
 * and adds none during an additional upkeep step; {@link Placement#AFTER_THIS_COMBAT_PHASE} (Obeka, Splitter of
 * Seconds) after the current combat phase, before the rest of the turn.
 *
 * @param count     how many additional upkeep steps
 * @param placement where in the turn they are added
 */
public record AdditionalUpkeepStepEffect(DynamicAmount count, Placement placement) implements CardEffect {

    /** Where the additional upkeep steps are added. */
    public enum Placement {
        /** Directly after the current upkeep step. */
        AFTER_THIS_STEP,
        /** Directly after the current combat phase. */
        AFTER_THIS_COMBAT_PHASE
    }

    public AdditionalUpkeepStepEffect {
        Objects.requireNonNull(count, "count");
        Objects.requireNonNull(placement, "placement");
    }

    /** "You get an additional upkeep step after this step." */
    public AdditionalUpkeepStepEffect() {
        this(new Fixed(1), Placement.AFTER_THIS_STEP);
    }

    /** "You get [count] additional upkeep steps after this phase", said during combat. */
    public static AdditionalUpkeepStepEffect afterThisCombatPhase(DynamicAmount count) {
        return new AdditionalUpkeepStepEffect(count, Placement.AFTER_THIS_COMBAT_PHASE);
    }
}
