package com.github.laxika.magicalvibes.model.amount;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * The number of matching spells the triggering spell's caster cast this turn before it ("for each other instant and
 * sorcery spell you've cast before it this turn": Thousand-Year Storm). Read as the ability resolves from the caster's
 * spells cast this turn up to the triggering spell's place among them, which the stack entry keeps from the moment the
 * ability triggered ({@code StackEntry.getTriggeringSpellCastIndex()}), so a spell cast in response doesn't count. A
 * countered spell was still cast and counts. Zero when the entry has no triggering spell.
 *
 * @param filter the spells that count, or {@code null} for every spell
 */
public record SpellsCastBeforeTriggeringSpellThisTurn(CardPredicate filter) implements DynamicAmount {
}
