package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static boost of +{@code powerPerCreature}/+{@code toughnessPerCreature} for each other creature
 * with the same name as the boosted creature. When {@code onlyControlled} is set, only creatures controlled
 * by the source's controller are counted ("each other creature you control named ..."); otherwise every such
 * creature on the battlefield counts regardless of controller (Relentless Rats).
 *
 * <p>With a {@code null} {@code scope} the effect boosts only its own source. With a scope it is a
 * continuous effect of the source that boosts every creature in that scope matching {@code filter}
 * (Mirror Box's "each nontoken creature you control gets +1/+1 for each other creature you control
 * with the same name as that creature"), so it does not depend on the boosted creatures' abilities.
 */
public record BoostByOtherCreaturesWithSameNameEffect(int powerPerCreature, int toughnessPerCreature,
                                                      boolean onlyControlled, GrantScope scope,
                                                      PermanentPredicate filter) implements CardEffect {

    public BoostByOtherCreaturesWithSameNameEffect(int powerPerCreature, int toughnessPerCreature,
                                                   boolean onlyControlled) {
        this(powerPerCreature, toughnessPerCreature, onlyControlled, null, null);
    }

    public BoostByOtherCreaturesWithSameNameEffect(int powerPerCreature, int toughnessPerCreature) {
        this(powerPerCreature, toughnessPerCreature, false);
    }
}
