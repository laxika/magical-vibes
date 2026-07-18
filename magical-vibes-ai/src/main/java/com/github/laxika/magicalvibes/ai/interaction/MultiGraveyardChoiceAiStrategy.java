package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

/**
 * Answers multi-graveyard card selections: the AI takes the first cards up to the maximum.
 */
@Slf4j
class MultiGraveyardChoiceAiStrategy implements AiInteractionStrategy<PendingInteraction.MultiGraveyardChoice> {

    @Override
    public Class<PendingInteraction.MultiGraveyardChoice> handledType() {
        return PendingInteraction.MultiGraveyardChoice.class;
    }

    @Override
    public void answer(PendingInteraction.MultiGraveyardChoice interaction, AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }

        List<UUID> validIds = interaction.validCardIds();
        if (validIds.isEmpty()) {
            return;
        }

        List<UUID> chosen = validIds.stream().limit(interaction.maxCount()).toList();

        log.info("AI: Choosing {} graveyard cards in game {}", chosen.size(), ctx.gameId());
        ctx.gameActions().answerInteraction(ctx.selfConnection(), new InteractionAnswer.CardsChosen(chosen));
    }
}
