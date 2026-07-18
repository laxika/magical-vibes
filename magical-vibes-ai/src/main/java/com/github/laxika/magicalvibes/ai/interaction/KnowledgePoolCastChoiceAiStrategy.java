package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

/**
 * Answers the Knowledge Pool cast choice: the AI always casts the first available card.
 */
@Slf4j
class KnowledgePoolCastChoiceAiStrategy implements AiInteractionStrategy<PendingInteraction.KnowledgePoolCastChoice> {

    @Override
    public Class<PendingInteraction.KnowledgePoolCastChoice> handledType() {
        return PendingInteraction.KnowledgePoolCastChoice.class;
    }

    @Override
    public void answer(PendingInteraction.KnowledgePoolCastChoice interaction, AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }

        List<UUID> chosen = interaction.validCardIds().stream().limit(1).toList();
        log.info("AI: Choosing card from Knowledge Pool in game {}", ctx.gameId());
        ctx.gameActions().answerInteraction(ctx.selfConnection(), new InteractionAnswer.CardsChosen(chosen));
    }
}
