package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/** Chooses an eligible creature card exiled with Koh for ability copying. */
@Slf4j
class KohExiledCreatureChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.KohExiledCreatureChoice> {

    @Override
    public Class<PendingInteraction.KohExiledCreatureChoice> handledType() {
        return PendingInteraction.KohExiledCreatureChoice.class;
    }

    @Override
    public void answer(PendingInteraction.KohExiledCreatureChoice interaction,
                       AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId()) || interaction.validCardIds().isEmpty()) {
            return;
        }

        log.info("AI: Choosing an exiled creature for Koh in game {}", ctx.gameId());
        ctx.gameActions().answerInteraction(
                new InteractionAnswer.CardsChosen(List.of(interaction.validCardIds().getFirst())));
    }
}
