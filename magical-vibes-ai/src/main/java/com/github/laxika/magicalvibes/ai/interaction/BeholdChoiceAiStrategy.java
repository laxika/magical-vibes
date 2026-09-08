package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

import java.util.List;

/** Chooses the first legal object when the AI must behold one. */
class BeholdChoiceAiStrategy implements AiInteractionStrategy<PendingInteraction.BeholdChoice> {

    @Override
    public Class<PendingInteraction.BeholdChoice> handledType() {
        return PendingInteraction.BeholdChoice.class;
    }

    @Override
    public void answer(PendingInteraction.BeholdChoice interaction, AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId()) || interaction.validCardIds().isEmpty()) {
            return;
        }
        ctx.gameActions().answerInteraction(
                new InteractionAnswer.CardsChosen(List.of(interaction.validCardIds().getFirst())));
    }
}
