package com.github.laxika.magicalvibes.model;

/**
 * Progress state for Forbidden Ritual's "sacrifice a nontoken permanent; if you do, target opponent
 * loses N life unless they sacrifice a permanent or discard; you may repeat" flow. Driven by
 * {@code ForbiddenRitualEffectHandler}, which re-runs on each controller sacrifice, opponent
 * penalty choice, and opponent sacrifice/discard completion (via
 * {@code rerunCurrentEffectAfterInteraction}). The optional repeat is a separate
 * {@code PendingInteraction.ForbiddenRitualRepeatChoice}.
 */
public class ForbiddenRitualState {

    /** Whether a flow is in progress (guards fresh initialization). */
    public boolean active;
    /**
     * True after the controller has sacrificed this cycle. Distinguishes a re-entry after the
     * controller's sacrifice (offer opponent penalty) from a re-entry after the opponent's
     * sacrifice/discard (offer the optional repeat).
     */
    public boolean controllerSacrificed;
    /** Life loss amount for the current resolution (copied from the effect). */
    public int lifeLoss;
    /** The penalty option the targeted opponent picked, consumed on the next re-entry. */
    public String chosenMode;

    public void reset() {
        active = false;
        controllerSacrificed = false;
        lifeLoss = 0;
        chosenMode = null;
    }
}
