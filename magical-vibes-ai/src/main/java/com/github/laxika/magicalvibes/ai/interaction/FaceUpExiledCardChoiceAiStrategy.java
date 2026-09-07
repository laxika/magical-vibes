package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

import java.util.List;

/** Chooses an eligible face-up exiled card when the effect offers one. */
class FaceUpExiledCardChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.FaceUpExiledCardChoice> {

    @Override
    public Class<PendingInteraction.FaceUpExiledCardChoice> handledType() {
        return PendingInteraction.FaceUpExiledCardChoice.class;
    }

    @Override
    public void answer(PendingInteraction.FaceUpExiledCardChoice interaction,
                       AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }

        ctx.gameActions().answerInteraction(new InteractionAnswer.CardsChosen(
                interaction.validCardIds().isEmpty()
                        ? List.of()
                        : List.of(interaction.validCardIds().getFirst())));
    }
}
