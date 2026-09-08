package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.Set;

/** Makes the source permanent a copy of a target battlefield creature or creature card in a graveyard. */
public record BecomeCopyOfTargetCreatureOrGraveyardCreatureCardUntilYourNextTurnEffect(
        String nameOverride,
        Set<CardSubtype> additionalSubtypesOverride,
        Set<CardType> additionalTypesOverride,
        Set<CardSupertype> additionalSupertypesOverride,
        Set<Keyword> additionalKeywordsOverride
) implements CardEffect {

    public BecomeCopyOfTargetCreatureOrGraveyardCreatureCardUntilYourNextTurnEffect {
        additionalSubtypesOverride = additionalSubtypesOverride == null
                ? Set.of() : Set.copyOf(additionalSubtypesOverride);
        additionalTypesOverride = additionalTypesOverride == null
                ? Set.of() : Set.copyOf(additionalTypesOverride);
        additionalSupertypesOverride = additionalSupertypesOverride == null
                ? Set.of() : Set.copyOf(additionalSupertypesOverride);
        additionalKeywordsOverride = additionalKeywordsOverride == null
                ? Set.of() : Set.copyOf(additionalKeywordsOverride);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.anyOf(
                TargetPredicates.creature(),
                TargetPredicates.graveyardCards(
                        new CardTypePredicate(CardType.CREATURE),
                        GraveyardSearchScope.ALL_GRAVEYARDS)));
    }
}
