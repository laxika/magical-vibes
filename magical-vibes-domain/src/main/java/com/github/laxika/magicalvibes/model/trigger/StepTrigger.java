package com.github.laxika.magicalvibes.model.trigger;

import java.util.Objects;

/**
 * A step or phase begins: an "at the beginning of" trigger (CR 603.2b). Build it through {@link Triggers}.
 *
 * @param moment the step or phase whose beginning it triggers on
 * @param player whose step or phase it must be
 */
public record StepTrigger(Moment moment, ActivePlayer player) implements Trigger {

    public StepTrigger {
        Objects.requireNonNull(moment, "moment");
        Objects.requireNonNull(player, "player");
    }

    /** The step and phase readers read this trigger's abilities themselves and judge their options. */
    @Override
    public boolean collectedAsAbility() {
        return true;
    }

    /** The beginnings of steps and phases a triggered ability can trigger on. */
    public enum Moment {
        /** The upkeep step. */
        UPKEEP,
        /** The draw step. */
        DRAW_STEP,
        /** The precombat main phase, the first main phase of the turn (CR 505.1). */
        PRECOMBAT_MAIN_PHASE,
        /** The beginning of combat step: "at the beginning of combat". */
        BEGINNING_OF_COMBAT,
        /** The end of combat step: "at end of combat". */
        END_OF_COMBAT,
        /** Every postcombat main phase. */
        POSTCOMBAT_MAIN_PHASE,
        /** The turn's second main phase (CR 505.1), and no later one. */
        SECOND_MAIN_PHASE,
        /** The end step. */
        END_STEP
    }
}
