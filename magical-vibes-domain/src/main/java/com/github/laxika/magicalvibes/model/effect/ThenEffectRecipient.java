package com.github.laxika.magicalvibes.model.effect;

/**
 * Which player's "controller slot" the {@code thenEffect} of a {@link DestroyTargetPermanentThenEffect}
 * acts on. The then-effect is resolved with its own {@code CONTROLLER}-style recipient against an entry
 * whose controller is chosen here, so no then-effect needs its own "target permanent's controller"
 * variant.
 */
public enum ThenEffectRecipient {
    /** The spell/ability controller (you gain life, this permanent gets +X/+0). */
    CONTROLLER,
    /**
     * The controller of the destroyed permanent, snapshotted before it leaves the battlefield
     * (e.g. "its controller loses N life" / "its controller gets a poison counter").
     */
    TARGET_CONTROLLER,
    /**
     * The owner of the destroyed permanent (its original controller absent any control-changing
     * effect), snapshotted before it leaves the battlefield (e.g. Path of Peace: "its owner gains
     * 4 life"). Differs from {@link #TARGET_CONTROLLER} only when the destroyed creature was stolen.
     */
    TARGET_OWNER
}
