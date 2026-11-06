package com.github.laxika.magicalvibes.model.effect;

/**
 * Breeches's reflexive effect: copy the spell that caused its trigger on a won flip, or deal
 * damage equal to that spell's mana value to any target on a lost flip. The spell and its caster are read from the
 * stack entry's triggering spell, so the copy is made after that spell has left the stack too (CR 608.2h).
 */
public record FlipCoinCopyTriggeringSpellOrDealDamageEffect() implements CardEffect {
}
