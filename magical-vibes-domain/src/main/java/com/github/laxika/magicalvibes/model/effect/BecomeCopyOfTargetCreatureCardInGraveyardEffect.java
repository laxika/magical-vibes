package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.Set;

/**
 * Causes the source permanent to become a copy of a target creature card in its controller's
 * graveyard whose mana value equals the activation's X value, with configurable copy exceptions.
 * The activated ability carrying this effect is retained by the handler, so a copy of a copy keeps it.
 */
public record BecomeCopyOfTargetCreatureCardInGraveyardEffect(
        boolean retainSourceName,
        boolean addLegendarySupertype,
        Set<Keyword> additionalKeywords,
        int retainedSourceAbilityIndex) implements CardEffect {

    public BecomeCopyOfTargetCreatureCardInGraveyardEffect() {
        this(true, true, Set.of(), -1);
    }

    public BecomeCopyOfTargetCreatureCardInGraveyardEffect(
            boolean retainSourceName,
            boolean addLegendarySupertype,
            Set<Keyword> additionalKeywords) {
        this(retainSourceName, addLegendarySupertype, additionalKeywords, -1);
    }

    public BecomeCopyOfTargetCreatureCardInGraveyardEffect {
        additionalKeywords = Set.copyOf(additionalKeywords);
        if (retainedSourceAbilityIndex < -1) {
            throw new IllegalArgumentException("Retained source ability index must be non-negative or -1");
        }
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCards(
                new CardTypePredicate(CardType.CREATURE),
                GraveyardSearchScope.CONTROLLERS_GRAVEYARD));
    }
}
