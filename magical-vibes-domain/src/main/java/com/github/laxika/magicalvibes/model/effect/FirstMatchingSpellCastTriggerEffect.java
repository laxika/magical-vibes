package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.List;

/**
 * Controller spell-cast trigger for "whenever you cast your first spell matching a filter each turn".
 * The count covers every spell the controller has cast this turn, including spells cast before the
 * source entered the battlefield, so it is evaluated against the spell history rather than a
 * once-per-turn flag on the source. Used in the {@code ON_CONTROLLER_CASTS_SPELL} slot (Tuvasa the Sunlit).
 *
 * @param spellFilter     the spells that count toward and can trigger this ability
 * @param resolvedEffects effects to put on the stack when this triggers
 */
public record FirstMatchingSpellCastTriggerEffect(
        CardPredicate spellFilter,
        List<CardEffect> resolvedEffects
) implements CardEffect {
}
