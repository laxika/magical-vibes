package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

/**
 * Answers Intuition's search: the AI reveals the first cards of its library, which is as good a
 * pick as any without deck evaluation.
 */
@Slf4j
class IntuitionSearchChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.IntuitionSearchChoice> {

    @Override
    public Class<PendingInteraction.IntuitionSearchChoice> handledType() {
        return PendingInteraction.IntuitionSearchChoice.class;
    }

    @Override
    public void answer(PendingInteraction.IntuitionSearchChoice interaction, AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }

        List<UUID> chosen = interaction.validCardIds().subList(0, interaction.count());
        log.info("AI: Revealing {} cards for Intuition in game {}", chosen.size(), ctx.gameId());
        ctx.gameActions().answerInteraction(new InteractionAnswer.CardsChosen(List.copyOf(chosen)));
    }
}
