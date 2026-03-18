package com.github.laxika.magicalvibes.model.effect;

/**
 * Destroys the targeted permanent. Optionally creates a creature token for the
 * target's controller (e.g. Beast Within, Pongify, Rapid Hybridization).
 *
 * @param cannotBeRegenerated whether the target cannot be regenerated
 * @param tokenForController  if non-null, creates this token for the destroyed permanent's controller
 */
public record DestroyTargetPermanentEffect(
        boolean cannotBeRegenerated,
        CreateCreatureTokenEffect tokenForController
) implements CardEffect {

    public DestroyTargetPermanentEffect() {
        this(false, null);
    }

    public DestroyTargetPermanentEffect(boolean cannotBeRegenerated) {
        this(cannotBeRegenerated, null);
    }

    @Override
    public boolean canTargetPermanent() {
        return true;
    }

    @Override
    public boolean isDamageOrDestruction() {
        return true;
    }
}
