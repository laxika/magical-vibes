package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

/** Always searches the library too: every zone's cards are then offered, and the search may still find nothing. */
class AndOrSearchZonesChoiceAiStrategy implements AiInteractionStrategy<PendingInteraction.AndOrSearchZonesChoice> {

    @Override
    public Class<PendingInteraction.AndOrSearchZonesChoice> handledType() {
        return PendingInteraction.AndOrSearchZonesChoice.class;
    }

    @Override
    public void answer(PendingInteraction.AndOrSearchZonesChoice interaction, AiInteractionContext ctx)
            throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.decidingPlayerId())) {
            return;
        }
        ctx.gameActions().answerInteraction(new InteractionAnswer.MayAbilityChosen(true));
    }
}
