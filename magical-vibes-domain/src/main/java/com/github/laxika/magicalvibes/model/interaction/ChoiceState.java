package com.github.laxika.magicalvibes.model.interaction;

import com.github.laxika.magicalvibes.model.ChoiceContext;

import java.util.UUID;

public class ChoiceState {

    private final UUID playerId;
    private final UUID permanentId;
    private final UUID etbTargetId;
    private final ChoiceContext choiceContext;

    public ChoiceState(UUID playerId, UUID permanentId, UUID etbTargetId,
                            ChoiceContext choiceContext) {
        this.playerId = playerId;
        this.permanentId = permanentId;
        this.etbTargetId = etbTargetId;
        this.choiceContext = choiceContext;
    }

    public UUID playerId() {
        return playerId;
    }

    public UUID permanentId() {
        return permanentId;
    }

    public UUID etbTargetId() {
        return etbTargetId;
    }

    public ChoiceContext choiceContext() {
        return choiceContext;
    }

    public ChoiceState deepCopy() {
        return new ChoiceState(playerId, permanentId, etbTargetId, choiceContext);
    }
}
