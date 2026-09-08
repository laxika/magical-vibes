package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.CardSubtype;

/** The controller sacrificed at least {@code minimum} permanents of the given subtype this turn. */
public record ControllerSacrificedPermanentSubtypeAtLeastThisTurn(int minimum, CardSubtype subtype)
        implements Condition {

    @Override
    public String conditionName() {
        return minimum + " or more " + subtype.getDisplayName() + "s sacrificed this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "fewer than " + minimum + " " + subtype.getDisplayName() + "s sacrificed this turn";
    }
}
