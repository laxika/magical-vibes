package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

import java.util.List;

/** Chooses the first card the AI may play from the group exiled by the effect. */
class ExiledCardMayPlayChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.ExiledCardMayPlayChoice> {

    @Override
    public Class<PendingInteraction.ExiledCardMayPlayChoice> handledType() {
        return PendingInteraction.ExiledCardMayPlayChoice.class;
    }

    @Override
    public void answer(PendingInteraction.ExiledCardMayPlayChoice interaction,
                       AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }
        List<java.util.UUID> chosen = interaction.validCardIds().isEmpty()
                ? List.of()
                : List.of(interaction.validCardIds().getFirst());
        ctx.gameActions().answerInteraction(new InteractionAnswer.CardsChosen(chosen));
    }
}
