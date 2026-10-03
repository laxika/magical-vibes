package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.CardSubtype;

/** At least one player was dealt combat damage this turn by a source with the requested subtype. */
public record AnyPlayerDealtCombatDamageBySubtypeThisTurn(CardSubtype subtype) implements Condition {

    @Override
    public String conditionName() {
        return "a player was dealt combat damage by a " + subtype.getDisplayName() + " this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "no player was dealt combat damage by a " + subtype.getDisplayName() + " this turn";
    }
}
