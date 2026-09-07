package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

/** Returns up to {@code maxTargets} target creature cards of the chosen type from the controller's graveyard to hand. */
public record ReturnTargetCreaturesOfChosenTypeFromGraveyardToHandEffect(int maxTargets)
        implements CardEffect, CastTimeCreatureTypeChoiceEffect {

    public ReturnTargetCreaturesOfChosenTypeFromGraveyardToHandEffect {
        if (maxTargets <= 0) {
            throw new IllegalArgumentException("maxTargets must be positive");
        }
    }

    public CardPredicate filter() {
        return new CardTypePredicate(CardType.CREATURE);
    }

    public CardPredicate filter(CardSubtype chosenCreatureType) {
        return new CardAllOfPredicate(List.of(
                filter(),
                new CardAnyOfPredicate(List.of(
                        new CardSubtypePredicate(chosenCreatureType),
                        new CardKeywordPredicate(Keyword.CHANGELING)))));
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCards(filter(),
                GraveyardSearchScope.CONTROLLERS_GRAVEYARD));
    }

    @Override
    public boolean requiresCastTimeCreatureTypeChoice() {
        return true;
    }
}
