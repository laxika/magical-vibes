package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Set;

/**
 * Chooses any number of creatures with different effective powers, then grants them double
 * strike until end of turn.
 */
public record ChooseCreaturesWithDifferentPowersGrantDoubleStrikeEffect()
        implements KeywordGrantingEffect {

    @Override
    public Set<Keyword> keywords() {
        return Set.of(Keyword.DOUBLE_STRIKE);
    }

    @Override
    public GrantScope scope() {
        return GrantScope.TARGETS;
    }

    @Override
    public PermanentPredicate filter() {
        return null;
    }
}
