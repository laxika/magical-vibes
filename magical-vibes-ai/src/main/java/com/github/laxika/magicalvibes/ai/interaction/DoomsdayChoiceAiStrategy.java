package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

/**
 * Answers the Doomsday choice: the AI keeps as many pool cards as allowed (up to five).
 */
@Slf4j
class DoomsdayChoiceAiStrategy implements AiInteractionStrategy<PendingInteraction.DoomsdayChoice> {

    @Override
    public Class<PendingInteraction.DoomsdayChoice> handledType() {
        return PendingInteraction.DoomsdayChoice.class;
    }

    @Override
    public void answer(PendingInteraction.DoomsdayChoice interaction, AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }

        List<UUID> chosen = interaction.validCardIds().stream().limit(interaction.maxCount()).toList();
        log.info("AI: Choosing {} cards for Doomsday in game {}", chosen.size(), ctx.gameId());
        ctx.gameActions().answerInteraction(ctx.selfConnection(), new InteractionAnswer.CardsChosen(chosen));
    }
}
