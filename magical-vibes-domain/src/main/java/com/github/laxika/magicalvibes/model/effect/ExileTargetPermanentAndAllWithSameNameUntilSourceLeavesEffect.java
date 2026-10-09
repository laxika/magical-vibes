package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Exile target nonland permanent and all other permanents with the same name until the source
 * leaves the battlefield, then return the exiled cards under their owners' control.
 * Used by Detention Sphere and Deputy of Detention. Tokens cease to exist on exile and are not
 * returned.
 *
 * @param sameNamePredicate optional filter for the other same-name permanents
 * @param sameNameOnlyTargetController whether to search only the target's controller's battlefield
 * @param returnsOnSourceLeavesTrigger whether the return happens through the source's
 *        leaves-the-battlefield triggered ability (Detention Sphere, which returns the cards
 *        simultaneously when that ability resolves) rather than immediately when the source leaves
 *        (Deputy of Detention)
 */
public record ExileTargetPermanentAndAllWithSameNameUntilSourceLeavesEffect(
        PermanentPredicate sameNamePredicate,
        boolean sameNameOnlyTargetController,
        boolean returnsOnSourceLeavesTrigger) implements CardEffect {

    public ExileTargetPermanentAndAllWithSameNameUntilSourceLeavesEffect(
            PermanentPredicate sameNamePredicate, boolean sameNameOnlyTargetController) {
        this(sameNamePredicate, sameNameOnlyTargetController, false);
    }

    public ExileTargetPermanentAndAllWithSameNameUntilSourceLeavesEffect() {
        this(null, false, true);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.permanent(),
                new PermanentNotPredicate(new PermanentIsLandPredicate()));
    }
}
