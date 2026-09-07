package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;

/**
 * Returns targeted cards from the controller's graveyard, with a Bolas planeswalker changing the
 * resolution from an opponent's choice to returning every targeted card.
 */
public record DeliverUntoEvilEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCard(GraveyardSearchScope.CONTROLLERS_GRAVEYARD));
    }
}
