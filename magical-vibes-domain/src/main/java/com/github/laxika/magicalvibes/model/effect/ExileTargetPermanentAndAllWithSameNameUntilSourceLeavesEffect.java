package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Exile target nonland permanent and all other permanents with the same name until the source
 * leaves the battlefield, then return the exiled cards under their owners' control.
 * Used by Detention Sphere. Tokens cease to exist on exile and are not returned.
 *
 * @param sameNamePredicate optional filter for the other same-name permanents
 * @param sameNameOnlyTargetController whether to search only the target's controller's battlefield
 */
public record ExileTargetPermanentAndAllWithSameNameUntilSourceLeavesEffect(
        PermanentPredicate sameNamePredicate,
        boolean sameNameOnlyTargetController) implements CardEffect {

    public ExileTargetPermanentAndAllWithSameNameUntilSourceLeavesEffect() {
        this(null, false);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.permanent(),
                new PermanentNotPredicate(new PermanentIsLandPredicate()));
    }
}
