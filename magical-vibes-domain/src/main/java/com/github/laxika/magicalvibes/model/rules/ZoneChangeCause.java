package com.github.laxika.magicalvibes.model.rules;

import com.github.laxika.magicalvibes.model.DepartureCause;

/** Why a card or permanent changed zones, for triggers that care how it moved. */
public enum ZoneChangeCause {
    /** No more specific cause applies. */
    OTHER,
    /** Destroyed by a spell, an ability or a state-based action (CR 701.8a). */
    DESTROY,
    /** Sacrificed by its controller (CR 701.21a). */
    SACRIFICE,
    /** Put into a graveyard by a state-based action that isn't destruction, such as 0 toughness. */
    STATE_BASED,
    /** Discarded from a hand. */
    DISCARD,
    /** Milled from a library. */
    MILL,
    /** Moved to the stack to be cast (CR 601.2a). */
    CAST,
    /** Exiled to pay the cost of a craft ability, as the permanent with it or as a material (CR 702.167a). */
    CRAFT;

    /** The zone-change cause of a battlefield departure performed for {@code cause}. */
    public static ZoneChangeCause of(DepartureCause cause) {
        return switch (cause) {
            case DESTROY, STATE_BASED_DESTROY -> DESTROY;
            case SACRIFICE -> SACRIFICE;
            case STATE_BASED -> STATE_BASED;
            case OTHER -> OTHER;
        };
    }
}
