package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

/** Chooses the first presented planar die result to ignore. */
class PlanarDieChoiceAiStrategy implements AiInteractionStrategy<PendingInteraction.PlanarDieChoice> {

    @Override
    public Class<PendingInteraction.PlanarDieChoice> handledType() {
        return PendingInteraction.PlanarDieChoice.class;
    }

    @Override
    public void answer(PendingInteraction.PlanarDieChoice interaction, AiInteractionContext ctx) {
        if (!ctx.aiPlayerId().equals(interaction.decidingPlayerId())) {
            return;
        }
        ctx.gameActions().answerInteraction(new InteractionAnswer.ListChoiceMade(
                interaction.options().getFirst()));
    }
}
