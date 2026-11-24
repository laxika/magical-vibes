package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Answers the search of Intuition and Burning-Rune Demon: the AI reveals the first cards of the library (the first
 * of each name where the names must differ, or none when too few names are left), which is as good a pick as any
 * without deck evaluation.
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
        if (!ctx.aiPlayerId().equals(interaction.decidingPlayerId())) {
            return;
        }

        List<UUID> chosen = new ArrayList<>();
        Set<String> names = new HashSet<>();
        for (Card card : interaction.pool()) {
            if (chosen.size() < interaction.count()
                    && (!interaction.requireDifferentNames() || names.add(card.getName()))) {
                chosen.add(card.getId());
            }
        }
        if (interaction.requireDifferentNames() && chosen.size() < interaction.count()) {
            chosen.clear();
        }
        log.info("AI: Finding {} cards in a library search in game {}", chosen.size(), ctx.gameId());
        ctx.gameActions().answerInteraction(new InteractionAnswer.CardsChosen(List.copyOf(chosen)));
    }
}
