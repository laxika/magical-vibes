package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import lombok.extern.slf4j.Slf4j;

/**
 * Answers the top-or-bottom follow-up for the hand cards being returned to the library
 * (Dream Cache): always bottom — the cards were picked as the least useful, so keep them
 * away from the next draws.
 */
@Slf4j
class PutCardsFromHandOnLibraryDestinationChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.PutCardsFromHandOnLibraryDestinationChoice> {

    private static final String BOTTOM =
            PendingInteraction.PutCardsFromHandOnLibraryDestinationChoice.OPTIONS.get(1);

    @Override
    public Class<PendingInteraction.PutCardsFromHandOnLibraryDestinationChoice> handledType() {
        return PendingInteraction.PutCardsFromHandOnLibraryDestinationChoice.class;
    }

    @Override
    public void answer(PendingInteraction.PutCardsFromHandOnLibraryDestinationChoice interaction,
                       AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }
        log.info("AI: Putting the chosen cards on the bottom of the library in game {}", ctx.gameId());
        ctx.gameActions().answerInteraction(ctx.selfConnection(), new InteractionAnswer.ListChoiceMade(BOTTOM));
    }
}
