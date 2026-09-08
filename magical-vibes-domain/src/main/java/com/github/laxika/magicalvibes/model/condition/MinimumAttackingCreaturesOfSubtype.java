package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.CardSubtype;

/** At least {@code minimum} currently attacking creatures have the given subtype. */
public record MinimumAttackingCreaturesOfSubtype(int minimum, CardSubtype subtype) implements Condition {

    @Override
    public String conditionName() {
        return "attacking " + minimum + " or more " + subtype;
    }

    @Override
    public String conditionNotMetReason() {
        return "fewer than " + minimum + " attacking " + subtype;
    }
}
