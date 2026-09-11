package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

import java.util.Comparator;

/** Selects the highest-mana-value card from the legal hand and library search pool. */
class SearchHandAndOrLibraryChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.SearchHandAndOrLibraryChoice> {

    @Override
    public Class<PendingInteraction.SearchHandAndOrLibraryChoice> handledType() {
        return PendingInteraction.SearchHandAndOrLibraryChoice.class;
    }

    @Override
    public void answer(PendingInteraction.SearchHandAndOrLibraryChoice interaction,
                       AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }
        var chosen = interaction.pool().stream()
                .max(Comparator.comparingInt(Card::getManaValue))
                .stream().map(Card::getId).toList();
        ctx.gameActions().answerInteraction(new InteractionAnswer.CardsChosen(chosen));
    }
}
