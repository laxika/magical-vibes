package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: this creature can't attack or block alone.
 * <p>
 * Per CR 508.1b, a creature with this ability can't be the only creature
 * declared as an attacker. Per CR 509.1b, it can't be the only creature
 * declared as a blocker. Two or more creatures with this ability can
 * attack or block together.
 */
public record CantAttackOrBlockAloneEffect() implements CardEffect {
}
