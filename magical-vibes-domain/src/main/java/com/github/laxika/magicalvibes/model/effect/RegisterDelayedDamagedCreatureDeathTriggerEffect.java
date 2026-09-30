package com.github.laxika.magicalvibes.model.effect;

/**
 * Registers a delayed trigger for the rest of the turn: whenever a creature that was dealt damage
 * by this effect's source dies, the wrapped effect resolves.
 *
 * @param effect the effect resolved for each qualifying creature death
 * @param allControllers whether creatures controlled by any player qualify, rather than only
 *                       creatures controlled by the registering player
 */
public record RegisterDelayedDamagedCreatureDeathTriggerEffect(CardEffect effect,
                                                                boolean allControllers)
        implements CardEffect {

    public RegisterDelayedDamagedCreatureDeathTriggerEffect(CardEffect effect) {
        this(effect, false);
    }
}
