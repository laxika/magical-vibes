package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import lombok.extern.slf4j.Slf4j;

/**
 * Answers the top-or-bottom pick for a spell countered by Hinder: always the bottom, which buries
 * the countered card as deep as possible instead of handing it straight back on the next draw.
 */
@Slf4j
class CounteredSpellLibraryDestinationChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.CounteredSpellLibraryDestinationChoice> {

    private static final String BOTTOM =
            PendingInteraction.CounteredSpellLibraryDestinationChoice.OPTIONS.get(1);

    @Override
    public Class<PendingInteraction.CounteredSpellLibraryDestinationChoice> handledType() {
        return PendingInteraction.CounteredSpellLibraryDestinationChoice.class;
    }

    @Override
    public void answer(PendingInteraction.CounteredSpellLibraryDestinationChoice interaction,
                       AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }
        log.info("AI: Putting the countered card on the bottom of its owner's library in game {}", ctx.gameId());
        ctx.gameActions().answerInteraction(new InteractionAnswer.ListChoiceMade(BOTTOM));
    }
}
