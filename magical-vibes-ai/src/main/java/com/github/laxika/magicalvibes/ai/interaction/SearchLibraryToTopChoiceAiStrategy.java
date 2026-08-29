package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

/** Answers a search-to-top choice by selecting the maximum legal number of matching cards. */
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

        List<UUID> chosen = interaction.validCardIds().stream()
                .limit(interaction.maximumSelectionCount())
                .toList();
        log.info("AI: Putting {} cards on top of the library in game {}", chosen.size(), ctx.gameId());
        ctx.gameActions().answerInteraction(new InteractionAnswer.CardsChosen(chosen));
    }
}
