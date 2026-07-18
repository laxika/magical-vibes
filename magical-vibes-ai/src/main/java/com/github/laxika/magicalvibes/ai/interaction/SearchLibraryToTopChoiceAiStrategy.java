package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

/**
 * Answers the Goblin Recruiter choice: the AI puts all matching cards on top of its library.
 */
@Slf4j
class SearchLibraryToTopChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.SearchLibraryToTopChoice> {

    @Override
    public Class<PendingInteraction.SearchLibraryToTopChoice> handledType() {
        return PendingInteraction.SearchLibraryToTopChoice.class;
    }

    @Override
    public void answer(PendingInteraction.SearchLibraryToTopChoice interaction, AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }

        List<UUID> chosen = interaction.validCardIds();
        log.info("AI: Putting {} cards on top for Goblin Recruiter in game {}", chosen.size(), ctx.gameId());
        ctx.gameActions().answerInteraction(ctx.selfConnection(), new InteractionAnswer.CardsChosen(chosen));
    }
}
