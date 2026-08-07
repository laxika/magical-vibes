package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Static: "Once during each of your turns, you may cast a [filter] spell from your graveyard."
 * The spell is cast for its normal costs and obeys its own timing rules. Used by Gisa and Geralf.
 */
public record CastSpellFromGraveyardOncePerYourTurnEffect(CardPredicate filter)
        implements CastSpellsFromGraveyardPermission {

    @Override
    public boolean oncePerControllerTurn() {
        return true;
    }
}
