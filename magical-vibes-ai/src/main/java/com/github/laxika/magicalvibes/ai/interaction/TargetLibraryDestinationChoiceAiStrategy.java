package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

/** Keeps the targeted card at the offered non-bottom library position. */
class TargetLibraryDestinationChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.TargetLibraryDestinationChoice> {

    @Override
    public Class<PendingInteraction.TargetLibraryDestinationChoice> handledType() {
        return PendingInteraction.TargetLibraryDestinationChoice.class;
    }

    @Override
    public void answer(PendingInteraction.TargetLibraryDestinationChoice interaction,
                       AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }
        ctx.gameActions().answerInteraction(
                new InteractionAnswer.ListChoiceMade(interaction.firstOption()));
    }
}
