package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Drain-on-cast trigger descriptor: "Whenever an opponent casts a [filtered] spell, that player
 * loses {@code lifeLoss} life and you gain {@code lifeGain} life." Used by Yawgmoth's Edict.
 * <p>
 * Placed in the {@code ON_OPPONENT_CASTS_SPELL} slot; the collector stamps the casting opponent as
 * the acting player (targetId) and builds the life change from the shared primitives — a
 * {@link LoseLifeEffect} with {@link LoseLifeRecipient#TARGET_PLAYER} plus a {@link GainLifeEffect}
 * for the controller (omitted when {@code lifeGain} is 0) — so no dedicated resolver is needed.
 *
 * @param lifeLoss    life the casting opponent loses
 * @param lifeGain    life the source's controller gains
 * @param spellFilter optional filter for which spells trigger this (null = any spell)
 */
public record SpellCastLifeDrainEffect(int lifeLoss, int lifeGain, CardPredicate spellFilter) implements CardEffect {
}
