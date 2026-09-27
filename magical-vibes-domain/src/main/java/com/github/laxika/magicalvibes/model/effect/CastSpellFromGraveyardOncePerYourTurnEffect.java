package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CastingCost;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.List;

/**
 * Static: "Once during each of your turns, you may cast a [filter] spell from your graveyard."
 * The spell is cast for its normal costs and obeys its own timing rules. Used by Gisa and Geralf
 * and Kotis, Sibsig Champion.
 */
public record CastSpellFromGraveyardOncePerYourTurnEffect(
        CardPredicate filter, List<CastingCost> additionalCosts, boolean exileAfterResolution,
        int additionalGraveyardExileCount)
        implements CastSpellsFromGraveyardPermission {

    public CastSpellFromGraveyardOncePerYourTurnEffect(CardPredicate filter) {
        this(filter, List.of(), false, 0);
    }

    public CastSpellFromGraveyardOncePerYourTurnEffect(
            CardPredicate filter, List<? extends CastingCost> additionalCosts) {
        this(filter, List.copyOf(additionalCosts), false, 0);
    }

    public CastSpellFromGraveyardOncePerYourTurnEffect(
            CardPredicate filter, List<? extends CastingCost> additionalCosts,
            int additionalGraveyardExileCount) {
        this(filter, List.copyOf(additionalCosts), false, additionalGraveyardExileCount);
    }

    public CastSpellFromGraveyardOncePerYourTurnEffect(
            CardPredicate filter, List<? extends CastingCost> additionalCosts,
            boolean exileAfterResolution) {
        this(filter, List.copyOf(additionalCosts), exileAfterResolution, 0);
    }

    public CastSpellFromGraveyardOncePerYourTurnEffect {
        additionalCosts = additionalCosts == null ? List.of() : List.copyOf(additionalCosts);
        if (additionalGraveyardExileCount < 0) {
            throw new IllegalArgumentException("Additional graveyard exile count cannot be negative");
        }
    }

    @Override
    public boolean oncePerControllerTurn() {
        return true;
    }

    @Override
    public String additionalGraveyardExileLabel() {
        return additionalGraveyardExileCount > 0 ? "other cards" : null;
    }
}
