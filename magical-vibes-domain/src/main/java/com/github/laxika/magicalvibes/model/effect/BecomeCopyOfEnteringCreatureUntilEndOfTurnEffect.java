package com.github.laxika.magicalvibes.model.effect;

/**
 * Trigger marker for an optional ability that makes the source a copy of a creature that just
 * entered the battlefield until end of turn. The enter-trigger collector materializes the
 * entering permanent as the target of a {@link BecomeCopyOfTargetCreatureUntilEndOfTurnEffect}.
 */
public record BecomeCopyOfEnteringCreatureUntilEndOfTurnEffect() implements CardEffect {
}
