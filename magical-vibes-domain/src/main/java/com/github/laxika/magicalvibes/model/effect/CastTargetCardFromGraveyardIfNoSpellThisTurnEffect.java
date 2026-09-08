package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Offers the controller a choice to cast the targeted card from a graveyard while this effect
 * resolves, using its normal mana cost, only if they have not cast a spell this turn. A successful
 * cast also prevents that player from casting additional spells for the rest of the turn.
 */
public record CastTargetCardFromGraveyardIfNoSpellThisTurnEffect(
        CardPredicate filter,
        GraveyardSearchScope scope
) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCards(filter, scope));
    }
}
