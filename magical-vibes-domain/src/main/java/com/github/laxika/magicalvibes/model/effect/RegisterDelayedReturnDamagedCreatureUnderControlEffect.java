package com.github.laxika.magicalvibes.model.effect;

/**
 * {@code ON_DAMAGED_CREATURE_DIES} effect for Seraph: "Whenever a creature dealt damage by this
 * creature this turn dies, put that card onto the battlefield under your control at the beginning
 * of the next end step. Sacrifice the creature when you lose control of this creature."
 *
 * <p>On resolution the handler reads the dying creature's card id from the trigger entry
 * ({@code StackEntry.triggeringCardId}) and queues a {@code DelayedGraveyardToBattlefieldUnderControl}
 * keyed to the source Seraph so the returned creature is linked to it for the control-loss sacrifice.
 */
public record RegisterDelayedReturnDamagedCreatureUnderControlEffect() implements CardEffect {
}
