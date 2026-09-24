package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.CardSubtype;

/** The source permanent was crewed this turn by a creature with the given subtype. */
public record SourceWasCrewedBySubtypeThisTurn(CardSubtype subtype) implements Condition {

    @Override
    public String conditionName() {
        return "crewed by a " + subtype.name().toLowerCase() + " this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "source was not crewed by a " + subtype.name().toLowerCase() + " this turn";
    }
}
