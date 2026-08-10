package com.github.laxika.magicalvibes.model.effect;

/**
 * Destroys the targeted permanent. Optionally creates a creature token for the
 * target's controller (e.g. Beast Within, Pongify, Rapid Hybridization).
 *
 * @param cannotBeRegenerated whether the target cannot be regenerated
 * @param tokenForController  if non-null, creates this token for the destroyed permanent's controller
 * @param targetGroup         activated-ability target-group index, or {@code -1} for the primary target
 */
public record DestroyTargetPermanentEffect(
        boolean cannotBeRegenerated,
        CreateTokenEffect tokenForController,
        int targetGroup
) implements RemovalEffect {

    public DestroyTargetPermanentEffect() {
        this(false, null, -1);
    }

    public DestroyTargetPermanentEffect(boolean cannotBeRegenerated) {
        this(cannotBeRegenerated, null, -1);
    }

    public DestroyTargetPermanentEffect(boolean cannotBeRegenerated, CreateTokenEffect tokenForController) {
        this(cannotBeRegenerated, tokenForController, -1);
    }

    public static DestroyTargetPermanentEffect forTargetGroup(int targetGroup) {
        return new DestroyTargetPermanentEffect(false, null, targetGroup);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.permanent());
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.DESTROY;
    }
}
