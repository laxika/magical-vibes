package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

import java.util.stream.IntStream;

/** Keeps the revealed order when bottoming cards found before a planar card. */
class PlanarDeckPlaneswalkCardOrderAiStrategy
        implements AiInteractionStrategy<PendingInteraction.PlanarDeckPlaneswalkCardOrder> {

    @Override
    public Class<PendingInteraction.PlanarDeckPlaneswalkCardOrder> handledType() {
        return PendingInteraction.PlanarDeckPlaneswalkCardOrder.class;
    }

    @Override
    public void answer(PendingInteraction.PlanarDeckPlaneswalkCardOrder interaction,
                       AiInteractionContext ctx) throws Exception {
        if (ctx.aiPlayerId().equals(interaction.playerId())) {
            ctx.gameActions().answerInteraction(new InteractionAnswer.CardOrder(
                    IntStream.range(0, interaction.cardsToBottom().size()).boxed().toList()));
        }
    }
}
