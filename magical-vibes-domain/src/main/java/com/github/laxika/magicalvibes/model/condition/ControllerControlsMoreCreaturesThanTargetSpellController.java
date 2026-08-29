package com.github.laxika.magicalvibes.model.condition;

/** This effect's controller controls more creatures than the targeted spell's controller. */
public record ControllerControlsMoreCreaturesThanTargetSpellController() implements Condition {

    @Override
    public String conditionName() {
        return "you control more creatures than the targeted spell's controller";
    }

    @Override
    public String conditionNotMetReason() {
        return "you do not control more creatures than the targeted spell's controller";
    }
}
