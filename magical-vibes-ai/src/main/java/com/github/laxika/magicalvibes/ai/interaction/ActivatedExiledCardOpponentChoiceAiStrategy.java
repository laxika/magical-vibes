package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

import java.util.List;

/** Chooses the first eligible opponent to select an exiled card. */
class ActivatedExiledCardOpponentChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.ActivatedExiledCardOpponentChoice> {

    @Override
    public Class<PendingInteraction.ActivatedExiledCardOpponentChoice> handledType() {
        return PendingInteraction.ActivatedExiledCardOpponentChoice.class;
    }

    @Override
    public void answer(PendingInteraction.ActivatedExiledCardOpponentChoice interaction,
                       AiInteractionContext ctx) throws Exception {
        if (ctx.aiPlayerId().equals(interaction.controllerId()) && !interaction.opponentIds().isEmpty()) {
            ctx.gameActions().answerInteraction(new InteractionAnswer.PermanentsChosen(
                    List.of(interaction.opponentIds().getFirst())));
        }
    }
}
