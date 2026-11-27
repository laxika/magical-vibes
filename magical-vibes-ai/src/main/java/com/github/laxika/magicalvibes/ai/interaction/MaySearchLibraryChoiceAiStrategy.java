package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

/** Always searches: the search itself may still find nothing, and what it may find is decided in the search. */
class MaySearchLibraryChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.MaySearchLibraryChoice> {

    @Override
    public Class<PendingInteraction.MaySearchLibraryChoice> handledType() {
        return PendingInteraction.MaySearchLibraryChoice.class;
    }

    @Override
    public void answer(PendingInteraction.MaySearchLibraryChoice interaction, AiInteractionContext ctx)
            throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }
        ctx.gameActions().answerInteraction(new InteractionAnswer.MayAbilityChosen(true));
    }
}
