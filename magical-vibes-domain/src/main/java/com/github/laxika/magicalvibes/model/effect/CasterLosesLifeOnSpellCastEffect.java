package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Trigger descriptor for "whenever a player casts a [filter] spell, that player loses N life"
 * (Soot Imp). Lives in the {@code ON_ANY_PLAYER_CASTS_SPELL} slot; the collector filters the cast
 * spell by {@code spellFilter} and queues a {@link LoseLifeEffect} with recipient
 * {@link LoseLifeRecipient#TARGET_PLAYER} whose target is preset to the casting player — so the
 * loss falls on the caster ("that player"), never a chosen target.
 *
 * @param spellFilter what spells trigger this (null = any spell)
 * @param amount      life the casting player loses
 */
public record CasterLosesLifeOnSpellCastEffect(CardPredicate spellFilter, int amount) implements CardEffect {
}
