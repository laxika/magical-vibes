package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.AbilityZones;

import java.util.Objects;

/**
 * Something happens to a plane or phenomenon card in a Planechase game: "when you planeswalk to this plane",
 * "whenever chaos ensues" (CR 311.7), "when you encounter this phenomenon" (CR 312.5). Build it through
 * {@link Triggers}.
 *
 * @param event what happens
 */
public record PlanarTrigger(PlanarEvent event) implements Trigger {

    public PlanarTrigger {
        Objects.requireNonNull(event, "event");
    }

    /**
     * The command zone: the abilities of a face-up plane or phenomenon card function from there (CR 901.7). A chaos
     * ability can also trigger for a revealed card still in the planar deck (CR 311.7); its readers find it there.
     */
    @Override
    public AbilityZones defaultZones() {
        return AbilityZones.COMMAND;
    }

    /**
     * The planar reader ({@code PlanechaseService}) reads this trigger's abilities itself
     * ({@code MiscAbilityReader}), so it has no legacy view.
     */
    @Override
    public boolean collectedAsAbility() {
        return true;
    }

    /** What happens to a planar card. */
    public enum PlanarEvent {
        /** The planar controller planeswalks to this plane: it is turned face up (CR 701.31b). */
        PLANESWALKED_TO,
        /** The planar controller planeswalks away from this plane: it is turned face down (CR 701.31b). */
        PLANESWALKED_FROM,
        /** Chaos ensues (CR 311.7). */
        CHAOS_ENSUES,
        /** The planar controller encounters this phenomenon (CR 312.5). */
        ENCOUNTERED
    }
}
