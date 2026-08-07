package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Damage-on-cast trigger descriptor: "Whenever an opponent casts a [filtered] spell, this permanent
 * deals {@code damage} damage to that player." Used by Mindsparker.
 * <p>
 * Placed in the {@code ON_OPPONENT_CASTS_SPELL} slot; the collector stamps the casting opponent as
 * the acting player (targetId, non-targeting) and queues a {@link DealDamageToPlayersEffect} with
 * {@link DamageRecipient#TARGET_PLAYER}, so the damage goes through the normal damage system
 * (prevention, redirection, protection, infect) and is attributed to the source permanent. The
 * damage counterpart of {@link SpellCastLifeDrainEffect}; no dedicated resolver is needed.
 *
 * @param damage      damage dealt to the casting opponent
 * @param spellFilter optional filter for which spells trigger this (null = any spell)
 * @param intervening optional intervening-"if" condition (CR 603.4) checked against the source
 *                    permanent both when the spell is cast and again as the ability resolves —
 *                    "…, if this creature is renowned, …" (Scab-Clan Berserker). null = none
 */
public record SpellCastDamageToCasterEffect(int damage, CardPredicate spellFilter, Condition intervening)
        implements CardEffect {

    /** The unconditional form: the trigger fires on every matching opponent cast. */
    public SpellCastDamageToCasterEffect(int damage, CardPredicate spellFilter) {
        this(damage, spellFilter, null);
    }
}
