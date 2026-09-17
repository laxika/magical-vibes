package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import lombok.extern.slf4j.Slf4j;

import java.util.stream.IntStream;

/** Answers Zyym's hand-order choice with the presented order. */
@Slf4j
class TargetPlayerHandOrderChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.TargetPlayerHandOrderChoice> {

    @Override
    public Class<PendingInteraction.TargetPlayerHandOrderChoice> handledType() {
        return PendingInteraction.TargetPlayerHandOrderChoice.class;
    }

    @Override
    public void answer(PendingInteraction.TargetPlayerHandOrderChoice interaction,
                       AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }
        ctx.gameActions().answerInteraction(new InteractionAnswer.CardOrder(
                IntStream.range(0, interaction.cards().size()).boxed().toList()));
    }
}
