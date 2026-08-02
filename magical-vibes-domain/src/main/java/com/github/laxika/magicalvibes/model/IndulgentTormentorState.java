package com.github.laxika.magicalvibes.model;

/**
 * Progress state for Indulgent Tormentor's target-opponent choice and optional creature sacrifice.
 */
public class IndulgentTormentorState {

    public boolean active;
    public boolean waitingForSacrifice;
    public String chosenMode;

    public void reset() {
        active = false;
        waitingForSacrifice = false;
        chosenMode = null;
    }
}
