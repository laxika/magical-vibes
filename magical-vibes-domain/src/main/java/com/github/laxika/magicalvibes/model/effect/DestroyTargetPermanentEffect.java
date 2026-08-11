package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Destroys the targeted permanent. Optionally creates a creature token for the
 * target's controller (e.g. Beast Within, Pongify, Rapid Hybridization).
 *
 * @param cannotBeRegenerated whether the target cannot be regenerated
 * @param tokenForController  if non-null, creates this token for the destroyed permanent's controller
 * @param targetGroup         activated-ability target-group index, or {@code -1} for the primary target
 * @param targetFilter        optional predicate narrowing the permanent target
 */
public record DestroyTargetPermanentEffect(
        boolean cannotBeRegenerated,
        CreateTokenEffect tokenForController,
        int targetGroup,
        PermanentPredicate targetFilter
) implements RemovalEffect {

    public DestroyTargetPermanentEffect() {
        this(false, null, -1, null);
    }

    public DestroyTargetPermanentEffect(boolean cannotBeRegenerated) {
        this(cannotBeRegenerated, null, -1, null);
    }

    public DestroyTargetPermanentEffect(boolean cannotBeRegenerated, CreateTokenEffect tokenForController) {
        this(cannotBeRegenerated, tokenForController, -1, null);
    }

    public static DestroyTargetPermanentEffect forTargetGroup(int targetGroup) {
        return new DestroyTargetPermanentEffect(false, null, targetGroup, null);
    }

    public DestroyTargetPermanentEffect(PermanentPredicate targetFilter) {
        this(false, null, -1, targetFilter);
    }

    @Override
    public TargetSpec targetSpec() {
        return targetFilter == null
                ? TargetSpec.harmful(TargetPredicates.permanent())
                : TargetSpec.harmful(TargetPredicates.permanent(), targetFilter);
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.DESTROY;
    }
}
