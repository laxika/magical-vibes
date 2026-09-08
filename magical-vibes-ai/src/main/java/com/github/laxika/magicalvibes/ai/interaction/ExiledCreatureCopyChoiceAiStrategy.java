package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/** Chooses the first eligible creature card exiled with the source permanent. */
@Slf4j
class ExiledCreatureCopyChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.ExiledCreatureCopyChoice> {

    @Override
    public Class<PendingInteraction.ExiledCreatureCopyChoice> handledType() {
        return PendingInteraction.ExiledCreatureCopyChoice.class;
    }

    @Override
    public void answer(PendingInteraction.ExiledCreatureCopyChoice interaction,
                       AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId()) || interaction.validCardIds().isEmpty()) {
            return;
        }

        log.info("AI: Choosing an exiled creature for a copy effect in game {}", ctx.gameId());
        ctx.gameActions().answerInteraction(
                new InteractionAnswer.CardsChosen(List.of(interaction.validCardIds().getFirst())));
    }
}
