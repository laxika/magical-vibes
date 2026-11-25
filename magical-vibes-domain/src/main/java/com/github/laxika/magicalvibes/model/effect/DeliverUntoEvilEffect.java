package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;

/**
 * Returns targeted cards from the controller's graveyard, with a Bolas planeswalker changing the
 * resolution from an opponent's choice to returning every targeted card.
 *
 * @param opponent the opponent who chooses the cards left in the graveyard, or {@code null} for the one the
 *                 controller chooses as it resolves (the one opponent, unasked)
 */
public record DeliverUntoEvilEffect(PlayerSubject opponent) implements CardEffect {

    public DeliverUntoEvilEffect() {
        this(null);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCard(GraveyardSearchScope.CONTROLLERS_GRAVEYARD));
    }
}
