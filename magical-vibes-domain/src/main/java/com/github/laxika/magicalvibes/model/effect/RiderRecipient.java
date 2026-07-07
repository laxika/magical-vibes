package com.github.laxika.magicalvibes.model.effect;

/**
 * Which player's "controller slot" the rider of a {@link DestroyTargetPermanentThenEffect} acts on.
 * The rider is resolved with its own {@code CONTROLLER}-style recipient against an entry whose
 * controller is chosen here, so no rider effect needs its own "target permanent's controller"
 * variant.
 */
public enum RiderRecipient {
    /** The spell/ability controller (you gain life, this permanent gets +X/+0). */
    CONTROLLER,
    /**
     * The controller of the destroyed permanent, snapshotted before it leaves the battlefield
     * (e.g. "its controller loses N life" / "its controller gets a poison counter").
     */
    TARGET_CONTROLLER
}
