package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect (Worship): while the controller controls a creature, damage that would reduce
 * their life total to less than 1 reduces it to 1 instead. This is a damage-only replacement —
 * it does not stop life loss, life payment, poison counters, or losing the game from being at 0
 * or less life through other means. The "controls a creature" condition is enforced by the engine
 * when damage is applied.
 */
public record DamageCantReduceLifeBelowOneEffect() implements CardEffect {
}
