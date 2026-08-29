package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CastingCost;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.List;

/**
 * Static effect that lets the controller cast matching spells from their graveyard for their
 * normal mana costs.
 */
public record CastSpellsFromGraveyardEffect(CardPredicate filter, List<CastingCost> additionalCosts,
                                            boolean onlyDuringControllerTurn)
        implements CastSpellsFromGraveyardPermission {

    public CastSpellsFromGraveyardEffect {
        additionalCosts = additionalCosts == null ? List.of() : List.copyOf(additionalCosts);
    }

    public CastSpellsFromGraveyardEffect(CardPredicate filter) {
        this(filter, List.of(), false);
    }
}
