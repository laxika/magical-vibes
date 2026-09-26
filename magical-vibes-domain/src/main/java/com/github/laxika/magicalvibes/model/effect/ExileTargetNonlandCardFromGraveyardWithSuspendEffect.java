package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

/** Exiles a target nonland card from your graveyard and registers it for suspend. */
public record ExileTargetNonlandCardFromGraveyardWithSuspendEffect(int timeCounters)
        implements GraveyardCardChoosingEffect {

    private static final CardPredicate NONLAND = new CardNotPredicate(new CardTypePredicate(CardType.LAND));

    public ExileTargetNonlandCardFromGraveyardWithSuspendEffect {
        if (timeCounters < 1) {
            throw new IllegalArgumentException("timeCounters must be positive");
        }
    }

    @Override
    public int graveyardChoiceMaxTargets() {
        return 1;
    }

    @Override
    public int graveyardChoiceMinTargets() {
        return 1;
    }

    @Override
    public CardPredicate graveyardChoiceFilter() {
        return NONLAND;
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCards(NONLAND,
                GraveyardSearchScope.CONTROLLERS_GRAVEYARD));
    }
}
