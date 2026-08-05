package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

/**
 * Exile target nonland permanent and all other permanents with the same name until the source
 * leaves the battlefield, then return the exiled cards under their owners' control.
 * Used by Detention Sphere. Tokens cease to exist on exile and are not returned.
 */
public record ExileTargetPermanentAndAllWithSameNameUntilSourceLeavesEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.permanent(),
                new PermanentNotPredicate(new PermanentIsLandPredicate()));
    }
}
