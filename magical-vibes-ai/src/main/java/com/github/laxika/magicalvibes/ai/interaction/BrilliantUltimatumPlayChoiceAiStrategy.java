package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Answers the Brilliant Ultimatum play choice: the AI plays every card from the chosen pile. The
 * engine plays lands (skipping any it can't, e.g. once the one-land-per-turn limit is reached) and
 * casts spells for free one at a time, pausing for target choices itself, so choosing all is safe.
 */
@Slf4j
class BrilliantUltimatumPlayChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.BrilliantUltimatumPlayChoice> {

    @Override
    public Class<PendingInteraction.BrilliantUltimatumPlayChoice> handledType() {
        return PendingInteraction.BrilliantUltimatumPlayChoice.class;
    }

    @Override
    public void answer(PendingInteraction.BrilliantUltimatumPlayChoice interaction, AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }

        List<UUID> chosen = new ArrayList<>(interaction.validCardIds());
        if (interaction.maxCount() > 0 && chosen.size() > interaction.maxCount()) {
            chosen = chosen.subList(0, interaction.maxCount());
        }
        log.info("AI: Playing {} cards for Brilliant Ultimatum in game {}", chosen.size(), ctx.gameId());
        ctx.gameActions().answerInteraction(ctx.selfConnection(), new InteractionAnswer.CardsChosen(chosen));
    }
}
