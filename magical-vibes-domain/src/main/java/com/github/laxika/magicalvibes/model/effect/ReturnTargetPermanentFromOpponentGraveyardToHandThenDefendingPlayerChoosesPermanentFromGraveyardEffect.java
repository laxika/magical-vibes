package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Returns the targeted permanent card from the damaged player's graveyard to its owner's hand,
 * then has that player choose a permanent card from the ability controller's graveyard to put
 * onto the battlefield under the ability controller's control.
 */
public record ReturnTargetPermanentFromOpponentGraveyardToHandThenDefendingPlayerChoosesPermanentFromGraveyardEffect(
        CardPredicate filter) implements CombatDamageTriggerContextEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCards(
                filter, GraveyardSearchScope.OPPONENT_GRAVEYARD));
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.DAMAGED_PLAYER;
    }
}
