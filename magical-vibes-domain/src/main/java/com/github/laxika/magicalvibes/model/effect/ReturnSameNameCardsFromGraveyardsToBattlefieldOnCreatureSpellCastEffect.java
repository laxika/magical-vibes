package com.github.laxika.magicalvibes.model.effect;

/**
 * Trigger descriptor for returning every card with the same name as a cast creature spell from
 * each player's graveyard to that player's battlefield.
 *
 * <p>The spell-cast trigger collector snapshots the spell name before creating the ordinary
 * graveyard-return effect, so the trigger retains the cast spell's name if the spell leaves the
 * stack before the trigger resolves.
 */
public record ReturnSameNameCardsFromGraveyardsToBattlefieldOnCreatureSpellCastEffect()
        implements CardEffect {
}
