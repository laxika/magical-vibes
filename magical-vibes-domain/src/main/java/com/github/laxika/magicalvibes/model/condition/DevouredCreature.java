package com.github.laxika.magicalvibes.model.condition;

/** The source permanent devoured at least one creature (CR 702.82) as it entered. */
public record DevouredCreature() implements Condition {

    @Override
    public String conditionName() {
        return "devoured a creature";
    }

    @Override
    public String conditionNotMetReason() {
        return "devoured no creatures";
    }
}
