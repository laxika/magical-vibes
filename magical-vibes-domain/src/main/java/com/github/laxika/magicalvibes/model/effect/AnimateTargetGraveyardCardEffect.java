package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;

import java.util.List;
import java.util.Set;

/** Temporarily changes the characteristics of a target permanent card in a graveyard. */
public record AnimateTargetGraveyardCardEffect(
        int power,
        int toughness,
        Set<CardColor> grantedColors,
        List<CardSubtype> grantedSubtypes,
        Set<CardType> grantedCardTypes
) implements CardEffect {

    public AnimateTargetGraveyardCardEffect {
        grantedColors = Set.copyOf(grantedColors);
        grantedSubtypes = List.copyOf(grantedSubtypes);
        grantedCardTypes = Set.copyOf(grantedCardTypes);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCards(
                new CardIsPermanentPredicate(), GraveyardSearchScope.ALL_GRAVEYARDS));
    }
}
