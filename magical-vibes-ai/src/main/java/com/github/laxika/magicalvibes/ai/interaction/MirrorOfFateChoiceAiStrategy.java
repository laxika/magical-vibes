package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

/**
 * Answers the Mirror of Fate choice: the AI keeps as many exiled cards as allowed.
 */
@Slf4j
class MirrorOfFateChoiceAiStrategy implements AiInteractionStrategy<PendingInteraction.MirrorOfFateChoice> {

    @Override
    public Class<PendingInteraction.MirrorOfFateChoice> handledType() {
        return PendingInteraction.MirrorOfFateChoice.class;
    }

    @Override
    public void answer(PendingInteraction.MirrorOfFateChoice interaction, AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }

        List<UUID> chosen = interaction.validCardIds().stream().limit(interaction.maxCount()).toList();
        log.info("AI: Choosing {} exiled cards for Mirror of Fate in game {}", chosen.size(), ctx.gameId());
        ctx.gameActions().answerInteraction(ctx.selfConnection(), new InteractionAnswer.CardsChosen(chosen));
    }
}
