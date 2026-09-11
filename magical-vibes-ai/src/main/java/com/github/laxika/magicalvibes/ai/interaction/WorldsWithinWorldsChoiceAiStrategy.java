package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

/** Chooses the available creatures to put onto the battlefield. */
class WorldsWithinWorldsChoiceAiStrategy implements AiInteractionStrategy<PendingInteraction.WorldsWithinWorldsChoice> {

    @Override
    public Class<PendingInteraction.WorldsWithinWorldsChoice> handledType() {
        return PendingInteraction.WorldsWithinWorldsChoice.class;
    }

    @Override
    public void answer(PendingInteraction.WorldsWithinWorldsChoice interaction, AiInteractionContext ctx) throws Exception {
        if (ctx.aiPlayerId().equals(interaction.playerId())) {
            ctx.gameActions().answerInteraction(new InteractionAnswer.CardsChosen(interaction.validCardIds()));
        }
    }
}
