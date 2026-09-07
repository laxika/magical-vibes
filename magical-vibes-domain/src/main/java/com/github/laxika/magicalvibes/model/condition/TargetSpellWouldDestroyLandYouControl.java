package com.github.laxika.magicalvibes.model.condition;

/** The targeted spell would destroy at least one land controlled by this condition's controller. */
public record TargetSpellWouldDestroyLandYouControl() implements Condition {

    @Override
    public String conditionName() {
        return "the target spell would destroy a land you control";
    }

    @Override
    public String conditionNotMetReason() {
        return "the target spell would not destroy a land you control";
    }
}
