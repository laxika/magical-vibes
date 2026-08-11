package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

import java.util.List;

/** Selects the first eligible revealed card for the current Vivid color. */
class VividCardChoiceAiStrategy implements AiInteractionStrategy<PendingInteraction.VividCardChoice> {

    @Override
    public Class<PendingInteraction.VividCardChoice> handledType() {
        return PendingInteraction.VividCardChoice.class;
    }

    @Override
    public void answer(PendingInteraction.VividCardChoice interaction,
                       AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }
        List<java.util.UUID> chosen = interaction.validCardIds().isEmpty()
                ? List.of()
                : List.of(interaction.validCardIds().getFirst());
        ctx.gameActions().answerInteraction(new InteractionAnswer.CardsChosen(chosen));
    }
}
