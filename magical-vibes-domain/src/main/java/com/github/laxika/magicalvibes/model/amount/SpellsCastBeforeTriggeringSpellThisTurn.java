package com.github.laxika.magicalvibes.model.amount;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * The number of matching spells cast this turn before the triggering spell: its caster's ("for each other instant and
 * sorcery spell you've cast before it this turn": Thousand-Year Storm), or with {@code anyPlayer} every player's
 * (storm, CR 702.40a: "copy it for each other spell that was cast before it this turn"). Read as the ability resolves
 * from the spells cast this turn up to the triggering spell's place among them, which the stack entry keeps from the
 * moment the ability triggered ({@code StackEntry.getTriggeringSpellCastIndex()} among the caster's,
 * {@code getTriggeringSpellTurnCastIndex()} among every player's), so a spell cast in response doesn't count. A
 * countered spell was still cast and counts. Zero when the entry has no triggering spell.
 *
 * @param filter    the spells that count, or {@code null} for every spell
 * @param anyPlayer whether every player's spells count, not only the triggering spell's caster's
 */
public record SpellsCastBeforeTriggeringSpellThisTurn(CardPredicate filter, boolean anyPlayer) implements DynamicAmount {

    /** The matching spells the triggering spell's caster cast before it this turn. */
    public SpellsCastBeforeTriggeringSpellThisTurn(CardPredicate filter) {
        this(filter, false);
    }

    /** Every spell any player cast before the triggering spell this turn: the storm count. */
    public static SpellsCastBeforeTriggeringSpellThisTurn byAnyPlayer() {
        return new SpellsCastBeforeTriggeringSpellThisTurn(null, true);
    }
}
