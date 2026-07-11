package com.github.laxika.magicalvibes.model.effect;

/**
 * Static evasion effect: this creature can't be blocked as long as it's attacking
 * alone (CR 509.1) — i.e. it is the only creature its controller declared as an attacker.
 */
public record CantBeBlockedIfAttackingAloneEffect() implements CardEffect {
}
