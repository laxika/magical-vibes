package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

class DrawFromLibraryPositionChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.DrawFromLibraryPositionChoice> {

    @Override
    public Class<PendingInteraction.DrawFromLibraryPositionChoice> handledType() {
        return PendingInteraction.DrawFromLibraryPositionChoice.class;
    }

    @Override
    public void answer(PendingInteraction.DrawFromLibraryPositionChoice interaction,
                       AiInteractionContext ctx) {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }
        ctx.gameActions().answerInteraction(new InteractionAnswer.NumberChosen(interaction.librarySize()));
    }
}
