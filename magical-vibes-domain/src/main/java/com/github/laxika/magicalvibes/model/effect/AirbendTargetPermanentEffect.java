package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Objects;

/**
 * Exiles the target permanent and lets its owner cast the card for {@code {2}} while it remains exiled.
 *
 * @param restriction what the permanent must be: a nonland permanent ("airbend target nonland permanent"), or a
 *                    creature ("airbend that creature", Monk Gyatso)
 */
public record AirbendTargetPermanentEffect(PermanentPredicate restriction) implements RemovalEffect {

    public AirbendTargetPermanentEffect {
        Objects.requireNonNull(restriction, "restriction");
    }

    /** "Airbend target nonland permanent." */
    public AirbendTargetPermanentEffect() {
        this(new PermanentNotPredicate(new PermanentIsLandPredicate()));
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.permanent(), restriction);
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.EXILE;
    }
}
