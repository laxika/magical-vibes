package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.StackEntry;

import java.util.UUID;

/**
 * Breeches's reflexive effect: copy the spell that caused its trigger on a won flip, or deal
 * damage equal to that spell's mana value to any target on a lost flip.
 */
public record FlipCoinCopyTriggeringSpellOrDealDamageEffect(
        StackEntry spellSnapshot,
        UUID castingPlayerId
) implements CardEffect {

    public FlipCoinCopyTriggeringSpellOrDealDamageEffect() {
        this(null, null);
    }
}
