package com.github.laxika.magicalvibes.model.effect;

/**
 * Registers a delayed trigger for the rest of the turn: whenever a creature that was dealt damage
 * by this effect's source dies, the wrapped effect resolves.
 *
 * @param effect the effect resolved for each qualifying creature death
 * @param allControllers whether creatures controlled by any player qualify, rather than only
 *                       creatures controlled by the registering player
 * @param damagedByTarget whether the damage source is the creature this spell targets ("whenever a
 *                        creature dealt damage by that creature dies this turn", Touch of Moonglove)
 *                        rather than the registering spell itself. That creature's damage history
 *                        is keyed by the permanent, so it still counts after the creature leaves
 *                        the battlefield and damage it dealt earlier this turn qualifies
 */
public record RegisterDelayedDamagedCreatureDeathTriggerEffect(CardEffect effect,
                                                                boolean allControllers,
                                                                boolean damagedByTarget)
        implements CardEffect {

    public RegisterDelayedDamagedCreatureDeathTriggerEffect(CardEffect effect, boolean allControllers) {
        this(effect, allControllers, false);
    }

    public RegisterDelayedDamagedCreatureDeathTriggerEffect(CardEffect effect) {
        this(effect, false, false);
    }

    /** "Whenever a creature dealt damage by that creature dies this turn" for the targeted creature. */
    public static RegisterDelayedDamagedCreatureDeathTriggerEffect byTargetCreature(CardEffect effect) {
        return new RegisterDelayedDamagedCreatureDeathTriggerEffect(effect, true, true);
    }
}
