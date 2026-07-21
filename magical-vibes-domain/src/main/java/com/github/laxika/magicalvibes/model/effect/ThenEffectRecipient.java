package com.github.laxika.magicalvibes.model.effect;

/**
 * Which player's "controller slot" the {@code thenEffect} of a destroy / exile / bounce-then effect
 * ({@link DestroyTargetPermanentThenEffect}, {@link ExileTargetPermanentThenEffect},
 * {@link ReturnTargetPermanentToHandThenEffect}) acts on. The then-effect is resolved with its own
 * {@code CONTROLLER}-style recipient against an entry whose controller is chosen here, so no
 * then-effect needs its own "target permanent's controller" variant.
 */
public enum ThenEffectRecipient {
    /** The spell/ability controller (you gain life, this permanent gets +X/+0). */
    CONTROLLER,
    /**
     * The controller of the removed permanent, snapshotted before it leaves the battlefield
     * (e.g. "its controller loses N life" / "its controller gets a poison counter").
     */
    TARGET_CONTROLLER,
    /**
     * The owner of the removed permanent (its original controller absent any control-changing
     * effect), snapshotted before it leaves the battlefield (e.g. Path of Peace: "its owner gains
     * 4 life"). Differs from {@link #TARGET_CONTROLLER} only when the permanent was stolen.
     */
    TARGET_OWNER,
    /**
     * The then-effect stays under the spell/ability controller but its <em>target</em> becomes the
     * removed permanent's controller (snapshotted before it leaves): the derived entry's
     * {@code targetId} is set to that player so a {@code TARGET_PLAYER}-recipient rider reads them
     * as the victim while damage-source shields still see the caster ("Cryoclasm deals 3 damage to
     * that land's controller").
     */
    TARGET_CONTROLLER_AS_TARGET,
    /**
     * Like {@link #TARGET_CONTROLLER_AS_TARGET}, but the rider's target is the removed permanent's
     * <em>owner</em> (snapshotted before it leaves). Used when the follow-up says "that player"
     * meaning the owner who received the card in hand (Compelling Deterrence: bounce, then that
     * player discards if you control a Zombie — the Zombie check stays on the caster).
     */
    TARGET_OWNER_AS_TARGET
}
