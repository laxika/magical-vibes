package com.github.laxika.magicalvibes.model.interaction;

import com.github.laxika.magicalvibes.model.ColorChoiceContext;

import java.util.UUID;

public class ColorChoiceState {

    private final UUID playerId;
    private final UUID permanentId;
    private final UUID etbTargetPermanentId;
    private final ColorChoiceContext choiceContext;

    public ColorChoiceState(UUID playerId, UUID permanentId, UUID etbTargetPermanentId,
                            ColorChoiceContext choiceContext) {
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

    public ColorChoiceContext choiceContext() {
        return choiceContext;
    }

    public ColorChoiceState deepCopy() {
        return new ColorChoiceState(playerId, permanentId, etbTargetPermanentId, choiceContext);
    }
}
