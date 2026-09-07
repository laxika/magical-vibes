package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/** Chooses the first eligible creature card exiled with Assimilation Aegis. */
@Slf4j
class AssimilationAegisCopyChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.AssimilationAegisCopyChoice> {

    @Override
    public Class<PendingInteraction.AssimilationAegisCopyChoice> handledType() {
        return PendingInteraction.AssimilationAegisCopyChoice.class;
    }

    @Override
    public void answer(PendingInteraction.AssimilationAegisCopyChoice interaction,
                       AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }
        List<java.util.UUID> valid = interaction.validCardIds();
        if (valid.isEmpty()) {
            return;
        }
        log.info("AI: Choosing a creature card exiled with Assimilation Aegis in game {}", ctx.gameId());
        ctx.gameActions().answerInteraction(new InteractionAnswer.CardsChosen(List.of(valid.getFirst())));
    }
}
