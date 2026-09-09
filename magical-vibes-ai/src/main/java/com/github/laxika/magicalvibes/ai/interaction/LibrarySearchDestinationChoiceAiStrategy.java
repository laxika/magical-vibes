package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

/** Keeps the searched card available to cast by putting it into hand. */
class LibrarySearchDestinationChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.LibrarySearchDestinationChoice> {

    @Override
    public Class<PendingInteraction.LibrarySearchDestinationChoice> handledType() {
        return PendingInteraction.LibrarySearchDestinationChoice.class;
    }

    @Override
    public void answer(PendingInteraction.LibrarySearchDestinationChoice interaction,
                       AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }
        ctx.gameActions().answerInteraction(new InteractionAnswer.ListChoiceMade(
                PendingInteraction.LibrarySearchDestinationChoice.OPTIONS.getFirst()));
    }
}
