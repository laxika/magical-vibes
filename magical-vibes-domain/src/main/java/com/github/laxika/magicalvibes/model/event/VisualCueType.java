package com.github.laxika.magicalvibes.model.event;

/**
 * What a {@link GameEventFact.VisualCue} reports happened. Cues are cosmetic: the client chooses
 * how each type looks, and no rules code ever reads them.
 */
public enum VisualCueType {
    /** A permanent was destroyed by a spell or ability. */
    DESTROYED,
    /** A permanent was sacrificed. */
    SACRIFICED,
    /** A permanent was exiled from the battlefield. */
    EXILED,
    /** A permanent was returned to its owner's hand. */
    RETURNED_TO_HAND,
    /**
     * A permanent was put into a graveyard from the battlefield other than by being destroyed by a
     * spell or ability or sacrificed: a state-based action such as lethal damage, zero toughness or
     * the legend rule.
     */
    DIED,
    /** Damage was dealt to a permanent; the amount is what was actually dealt. */
    DAMAGE_TO_PERMANENT,
    /** Damage was dealt to a player; the amount is what was actually dealt. */
    DAMAGE_TO_PLAYER,
    /** A spell was put onto the stack. */
    SPELL_CAST,
    /** A spell resolved. */
    SPELL_RESOLVED,
    /** A spell was countered. */
    SPELL_COUNTERED,
    /** A player gained life. */
    LIFE_GAINED,
    /** A player lost life other than from damage. */
    LIFE_LOST,
    /** Counters were put on a permanent. */
    COUNTERS_ADDED
}
