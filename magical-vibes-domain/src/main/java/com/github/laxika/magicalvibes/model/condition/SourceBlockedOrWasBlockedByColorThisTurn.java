package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.CardColor;

/**
 * True if the source permanent blocked or was blocked by a creature of the given color at any point this
 * turn (read from the turn-scoped combat-block tracking recorded at declare-blockers time). The other
 * creature's color is judged at the moment of the block, so the condition stays true after combat ends or
 * the other creature leaves the battlefield / changes color. Used by Sea Troll's regeneration ability.
 */
public record SourceBlockedOrWasBlockedByColorThisTurn(CardColor color) implements Condition {

    @Override
    public String conditionName() {
        return "source blocked or was blocked by a " + color.name().toLowerCase() + " creature this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "this creature has not blocked or been blocked by a " + color.name().toLowerCase()
                + " creature this turn";
    }
}
