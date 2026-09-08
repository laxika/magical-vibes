package com.github.laxika.magicalvibes.model.condition;

public record SourceStartedTurnUntapped() implements Condition {

    @Override
    public String conditionName() {
        return "source started the turn untapped";
    }

    @Override
    public String conditionNotMetReason() {
        return "source started the turn tapped";
    }
}
