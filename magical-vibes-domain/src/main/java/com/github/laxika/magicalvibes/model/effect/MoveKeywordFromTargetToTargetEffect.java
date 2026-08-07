package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Set;

/**
 * "Until end of turn, target creature with {keyword} loses it and another target creature gains it."
 * (Phyrexian Splicer).
 *
 * <p>Reads two targets from the flat multi-target list: position 0 loses {@code keyword}, position 1
 * gains it, both until end of turn. Each half resolves independently — if one target has left the
 * battlefield the other still happens. The targeting restriction ("a creature with the chosen
 * ability") is expressed by the ability's positional target filters, not here.
 */
public record MoveKeywordFromTargetToTargetEffect(Keyword keyword) implements KeywordGrantingEffect {

    @Override
    public Set<Keyword> keywords() {
        return Set.of(keyword);
    }

    @Override
    public GrantScope scope() {
        return GrantScope.TARGET;
    }

    @Override
    public PermanentPredicate filter() {
        return null;
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent());
    }
}
