package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.Objects;
import java.util.Set;

/** Conjures a duplicate of each targeted card exiled with the source into its controller's hand. */
public record ConjureDuplicateOfTargetExiledCardIntoHandEffect(
        CardPredicate filter,
        int powerBoost,
        int toughnessBoost,
        Set<Keyword> keywords,
        boolean anyColorMana,
        boolean drainOnEnter,
        int mayPutOntoBattlefieldManaValueAtMost
) implements CardEffect {

    public ConjureDuplicateOfTargetExiledCardIntoHandEffect {
        Objects.requireNonNull(filter, "filter");
        keywords = Set.copyOf(keywords);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.exiledCards(filter));
    }
}
