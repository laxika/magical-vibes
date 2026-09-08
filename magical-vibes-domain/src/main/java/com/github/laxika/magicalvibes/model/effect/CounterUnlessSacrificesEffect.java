package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

/**
 * Counters the targeted spell or ability unless its controller sacrifices a matching permanent.
 * This is the permanent-sacrifice variant of ward.
 *
 * @param filter the permanent filter for the sacrifice
 * @param sacrificeDescription the noun used in the choice prompt
 */
public record CounterUnlessSacrificesEffect(PermanentPredicate filter, String sacrificeDescription,
                                            int requiredCount)
        implements CounterUnlessEffect, TriggeringSpellReferencingEffect {

    /** Creates the unrestricted permanent-sacrifice variant used by ward. */
    public CounterUnlessSacrificesEffect() {
        this(new PermanentTruePredicate(), "permanent", 1);
    }

    /** Creates a sacrifice variant restricted to the supplied permanent filter. */
    public CounterUnlessSacrificesEffect(PermanentPredicate filter) {
        this(filter, "permanent", 1);
    }

    /** Creates a sacrifice variant requiring exactly the supplied number of permanents. */
    public CounterUnlessSacrificesEffect(PermanentPredicate filter, String sacrificeDescription) {
        this(filter, sacrificeDescription, 1);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.spellOnStack());
    }

    @Override
    public RansomKind ransomKind() {
        return RansomKind.SACRIFICE_PERMANENT;
    }

    @Override
    public int ransomMagnitude() {
        return requiredCount;
    }
}
