package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

import java.util.stream.IntStream;

/** Keeps the revealed order when putting non-plane cards on the bottom of the planar deck. */
class SpatialMergingCardOrderAiStrategy implements AiInteractionStrategy<PendingInteraction.SpatialMergingCardOrder> {

    @Override
    public Class<PendingInteraction.SpatialMergingCardOrder> handledType() {
        return PendingInteraction.SpatialMergingCardOrder.class;
    }

    @Override
    public void answer(PendingInteraction.SpatialMergingCardOrder interaction, AiInteractionContext ctx)
            throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }
        ctx.gameActions().answerInteraction(new InteractionAnswer.CardOrder(
                IntStream.range(0, interaction.cardsToBottom().size()).boxed().toList()));
    }
}
