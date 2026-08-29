package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

import java.util.List;

/** Chooses the first nonland card when an AI is the opponent making Plargg and Nassari's choice. */
class PlarggAndNassariCardChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.PlarggAndNassariCardChoice> {

    @Override
    public Class<PendingInteraction.PlarggAndNassariCardChoice> handledType() {
        return PendingInteraction.PlarggAndNassariCardChoice.class;
    }

    @Override
    public void answer(PendingInteraction.PlarggAndNassariCardChoice interaction,
                       AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.opponentId()) || interaction.validCardIds().isEmpty()) {
            return;
        }
        ctx.gameActions().answerInteraction(new InteractionAnswer.CardsChosen(
                List.of(interaction.validCardIds().getFirst())));
    }
}
