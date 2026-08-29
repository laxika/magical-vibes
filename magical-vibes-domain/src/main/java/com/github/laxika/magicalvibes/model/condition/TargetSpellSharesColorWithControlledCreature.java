package com.github.laxika.magicalvibes.model.condition;

/** The targeted spell shares a color with a creature controlled by the effect's controller. */
public record TargetSpellSharesColorWithControlledCreature() implements Condition {

    @Override
    public String conditionName() {
        return "target spell shares a color with a creature you control";
    }

    @Override
    public String conditionNotMetReason() {
        return "target spell does not share a color with a creature you control";
    }
}
