package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CastingCost;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.List;

/**
 * Static: "Once during each of your turns, you may cast a [filter] spell from your graveyard."
 * The spell is cast for its normal costs and obeys its own timing rules. Used by Gisa and Geralf.
 */
public record CastSpellFromGraveyardOncePerYourTurnEffect(
        CardPredicate filter, List<CastingCost> additionalCosts, boolean exileAfterResolution)
        implements CastSpellsFromGraveyardPermission {

    public CastSpellFromGraveyardOncePerYourTurnEffect(CardPredicate filter) {
        this(filter, List.of(), false);
    }

    public CastSpellFromGraveyardOncePerYourTurnEffect(
            CardPredicate filter, List<? extends CastingCost> additionalCosts) {
        this(filter, List.copyOf(additionalCosts), false);
    }

    public CastSpellFromGraveyardOncePerYourTurnEffect {
        additionalCosts = additionalCosts == null ? List.of() : List.copyOf(additionalCosts);
    }

    @Override
    public boolean oncePerControllerTurn() {
        return true;
    }
}
