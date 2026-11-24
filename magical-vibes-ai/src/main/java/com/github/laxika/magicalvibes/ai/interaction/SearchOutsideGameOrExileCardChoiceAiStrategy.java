package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Chooses the highest-mana-value eligible card from the AI's cards outside the game. */
class SearchOutsideGameOrExileCardChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.SearchOutsideGameOrExileCardChoice> {

    @Override
    public Class<PendingInteraction.SearchOutsideGameOrExileCardChoice> handledType() {
        return PendingInteraction.SearchOutsideGameOrExileCardChoice.class;
    }

    @Override
    public void answer(PendingInteraction.SearchOutsideGameOrExileCardChoice interaction,
                       AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }

        Set<UUID> validIds = Set.copyOf(interaction.validCardIds());
        List<UUID> chosen = ctx.gameData().playerSideboards.getOrDefault(ctx.aiPlayerId(), List.of()).stream()
                .filter(card -> validIds.contains(card.getId()))
                .max(Comparator.comparingInt(Card::getManaValue))
                .map(card -> List.of(card.getId()))
                .orElseGet(List::of);

        ctx.gameActions().answerInteraction(new InteractionAnswer.CardsChosen(chosen));
    }
}
