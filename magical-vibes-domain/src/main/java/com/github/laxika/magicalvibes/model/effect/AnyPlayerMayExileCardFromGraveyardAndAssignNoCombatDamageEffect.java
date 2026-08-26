package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.UUID;

/**
 * Offers each player the option to exile the specified number of cards from their graveyard. If a
 * player accepts, the source permanent assigns no combat damage this turn.
 *
 * @param cardsToExile number of cards a player must exile to accept the choice
 * @param remainingPlayerIds players who have not yet received the option
 * @param abilityControllerId controller of the triggered ability
 * @param sourcePermanentId permanent whose combat damage is suppressed
 */
public record AnyPlayerMayExileCardFromGraveyardAndAssignNoCombatDamageEffect(
        int cardsToExile,
        List<UUID> remainingPlayerIds,
        UUID abilityControllerId,
        UUID sourcePermanentId
) implements CardEffect {

    public AnyPlayerMayExileCardFromGraveyardAndAssignNoCombatDamageEffect() {
        this(1, null, null, null);
    }

    public AnyPlayerMayExileCardFromGraveyardAndAssignNoCombatDamageEffect(int cardsToExile) {
        this(cardsToExile, null, null, null);
    }
}
