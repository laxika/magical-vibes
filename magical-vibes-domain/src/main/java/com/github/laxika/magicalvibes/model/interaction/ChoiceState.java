package com.github.laxika.magicalvibes.model.interaction;

import com.github.laxika.magicalvibes.model.ChoiceContext;

import java.util.UUID;

public class ChoiceState {

    private final UUID playerId;
    private final UUID permanentId;
    private final UUID etbTargetPermanentId;
    private final ChoiceContext choiceContext;

    public ChoiceState(UUID playerId, UUID permanentId, UUID etbTargetPermanentId,
                            ChoiceContext choiceContext) {
        this.playerId = playerId;
        this.permanentId = permanentId;
        this.etbTargetPermanentId = etbTargetPermanentId;
        this.choiceContext = choiceContext;
    }

    public UUID playerId() {
        return playerId;
    }

    public UUID permanentId() {
        return permanentId;
    }

    public UUID etbTargetPermanentId() {
        return etbTargetPermanentId;
    }

    public ChoiceContext choiceContext() {
        return choiceContext;
    }

    public ChoiceState deepCopy() {
        return new ChoiceState(playerId, permanentId, etbTargetPermanentId, choiceContext);
    }
}
