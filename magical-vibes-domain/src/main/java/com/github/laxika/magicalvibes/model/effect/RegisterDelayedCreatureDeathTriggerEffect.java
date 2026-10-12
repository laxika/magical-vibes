package com.github.laxika.magicalvibes.model.effect;

/**
 * Registers a delayed triggered ability that resolves {@code effect} whenever a creature dies for
 * the rest of the turn. The delayed ability outlives the source card and is cleared at turn cleanup.
 *
 * @param effect      the effect resolved for each creature that dies
 * @param onlyOthers  true for "whenever another creature dies": the delayed ability ignores the death of the
 *                    source permanent that created it, and its triggers keep that permanent as their source
 *                    (last known information once it has left the battlefield)
 */
public record RegisterDelayedCreatureDeathTriggerEffect(CardEffect effect, boolean onlyOthers) implements CardEffect {

    public RegisterDelayedCreatureDeathTriggerEffect(CardEffect effect) {
        this(effect, false);
    }
}
