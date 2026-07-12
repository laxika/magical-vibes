package com.github.laxika.magicalvibes.model.effect;

/**
 * Persist (CR 702.79): "When this creature dies, if it had no -1/-1 counters on it, return it to the
 * battlefield under its owner's control with a -1/-1 counter on it."
 *
 * <p>This effect is the resolution half of the Persist triggered ability. The "if it had no -1/-1
 * counters" intervening-if check is evaluated when the trigger is collected (the creature has already
 * left the battlefield by then, so it uses last-known information). On resolution this effect finds the
 * dying card in its owner's graveyard and, if still there, returns it to the battlefield from the
 * graveyard with a single -1/-1 counter.
 */
public record PersistReturnEffect() implements CardEffect {
}
