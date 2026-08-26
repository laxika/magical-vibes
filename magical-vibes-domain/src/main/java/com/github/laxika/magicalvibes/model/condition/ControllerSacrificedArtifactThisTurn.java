package com.github.laxika.magicalvibes.model.condition;

/** The effect's controller sacrificed an artifact this turn. */
public record ControllerSacrificedArtifactThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "you sacrificed an artifact this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "you didn't sacrifice an artifact this turn";
    }
}
