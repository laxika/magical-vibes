package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Drain-on-cast trigger descriptor: "Whenever an opponent casts a [filtered] spell, that player
 * loses {@code lifeLoss} life and you gain {@code lifeGain} life." Used by Yawgmoth's Edict.
 * <p>
 * Placed in the {@code ON_OPPONENT_CASTS_SPELL} slot; the collector stamps the casting opponent as
 * the acting player and delegates the actual life change to
 * {@link TargetPlayerLosesLifeAndControllerGainsLifeEffect}, so no dedicated resolver is needed.
 *
 * @param lifeLoss    life the casting opponent loses
 * @param lifeGain    life the source's controller gains
 * @param spellFilter optional filter for which spells trigger this (null = any spell)
 */
public record SpellCastLifeDrainEffect(int lifeLoss, int lifeGain, CardPredicate spellFilter) implements CardEffect {
}
