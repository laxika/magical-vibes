package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Trigger descriptor for "Whenever you cast a [matching] spell, you may have
 * target creature get +X/+Y and gain [keyword] until end of turn."
 * <p>
 * Used in the {@code ON_ANY_PLAYER_CASTS_SPELL} slot wrapped in {@link MayEffect}.
 * {@code checkSpellCastTriggers} resolves this into a {@link BoostTargetCreatureEffect}
 * and a {@link GrantKeywordEffect} with {@link GrantScope#TARGET}.
 */
public record BoostAndGrantKeywordOnOwnSpellCastEffect(
        CardPredicate spellFilter,
        int powerBoost,
        int toughnessBoost,
        Keyword keyword
) implements CardEffect {
}
