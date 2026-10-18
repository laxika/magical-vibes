package com.github.laxika.magicalvibes.model.condition;

/**
 * The permanent that caused the trigger shares at least one creature type with the source
 * permanent. Used as an intervening-if so it is checked again on resolution (CR 603.4).
 */
public record TriggeringPermanentSharesCreatureTypeWithSource() implements Condition {

    @Override
    public String conditionName() {
        return "triggering permanent shares a creature type with source";
    }

    @Override
    public String conditionNotMetReason() {
        return "triggering permanent does not share a creature type with source";
    }
}
