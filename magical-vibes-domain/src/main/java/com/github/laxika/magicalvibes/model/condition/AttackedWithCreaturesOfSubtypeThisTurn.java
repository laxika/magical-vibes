package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.CardSubtype;

/** The controller declared at least {@code minimum} attacking creatures of the subtype this turn. */
public record AttackedWithCreaturesOfSubtypeThisTurn(int minimum, CardSubtype subtype) implements Condition {

    @Override
    public String conditionName() {
        return "attacked with " + minimum + " or more " + subtype;
    }

    @Override
    public String conditionNotMetReason() {
        return "you didn't attack with " + minimum + " or more " + subtype;
    }
}
