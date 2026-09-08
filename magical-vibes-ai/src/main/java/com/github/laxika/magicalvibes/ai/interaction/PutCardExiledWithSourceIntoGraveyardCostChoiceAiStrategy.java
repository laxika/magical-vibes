package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

import java.util.List;

/** Chooses the first card exiled with the source permanent for the activation cost. */
class PutCardExiledWithSourceIntoGraveyardCostChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.PutCardExiledWithSourceIntoGraveyardCostChoice> {

    @Override
    public Class<PendingInteraction.PutCardExiledWithSourceIntoGraveyardCostChoice> handledType() {
        return PendingInteraction.PutCardExiledWithSourceIntoGraveyardCostChoice.class;
    }

    @Override
    public void answer(PendingInteraction.PutCardExiledWithSourceIntoGraveyardCostChoice interaction,
                       AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId()) || interaction.validCardIds().isEmpty()) {
            return;
        }
        ctx.gameActions().answerInteraction(new InteractionAnswer.CardsChosen(
                List.of(interaction.validCardIds().getFirst())));
    }
}
