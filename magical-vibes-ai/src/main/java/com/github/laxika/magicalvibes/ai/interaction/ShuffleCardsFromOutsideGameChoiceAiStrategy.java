package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/** Chooses the highest-mana-value cards available to shuffle into the AI's library. */
class ShuffleCardsFromOutsideGameChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.ShuffleCardsFromOutsideGameChoice> {

    @Override
    public Class<PendingInteraction.ShuffleCardsFromOutsideGameChoice> handledType() {
        return PendingInteraction.ShuffleCardsFromOutsideGameChoice.class;
    }

    @Override
    public void answer(PendingInteraction.ShuffleCardsFromOutsideGameChoice interaction,
                       AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }

        List<UUID> chosen = interaction.pool().stream()
                .sorted(Comparator.comparingInt(Card::getManaValue).reversed())
                .limit(Math.max(0, interaction.maxCount()))
                .map(Card::getId)
                .toList();
        ctx.gameActions().answerInteraction(new InteractionAnswer.CardsChosen(chosen));
    }
}
