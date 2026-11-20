package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Set;

/**
 * On resolution, prompts for a basic land type and grants the matching landwalk until end of
 * turn to the permanent selected by {@code subject}.
 */
public record GrantChosenLandwalkEffect(PermanentSubject subject) implements KeywordGrantingEffect {

    public GrantChosenLandwalkEffect {
        if (!(subject instanceof PermanentSubject.Source) && !(subject instanceof PermanentSubject.Target)) {
            throw new IllegalArgumentException("GrantChosenLandwalkEffect supports only source() and target(), got "
                    + subject);
        }
    }

    @Override
    public Set<Keyword> keywords() {
        return Keyword.LANDWALK_MAP.keySet();
    }

    @Override
    public PermanentPredicate filter() {
        return null;
    }

    @Override
    public TargetSpec targetSpec() {
        return subject instanceof PermanentSubject.Target
                ? TargetSpec.benign(TargetPredicates.creature())
                : TargetSpec.NONE;
    }
}
