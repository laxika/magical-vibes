package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

/** Exiles up to one Saga card from a graveyard and copies one of its chapter abilities. */
public record ExileTargetSagaCardFromGraveyardAndCopyChapterEffect(int chapterNumber)
        implements GraveyardCardChoosingEffect {

    private static final CardPredicate SAGA_CARD = new CardSubtypePredicate(CardSubtype.SAGA);

    public ExileTargetSagaCardFromGraveyardAndCopyChapterEffect {
        if (chapterNumber < 1 || chapterNumber > 3) {
            throw new IllegalArgumentException("chapterNumber must be between 1 and 3");
        }
    }

    @Override
    public int graveyardChoiceMaxTargets() {
        return 1;
    }

    @Override
    public CardPredicate graveyardChoiceFilter() {
        return SAGA_CARD;
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCards(
                SAGA_CARD, GraveyardSearchScope.ALL_GRAVEYARDS));
    }

    @Override
    public boolean hasOptionalTarget() {
        return true;
    }
}
