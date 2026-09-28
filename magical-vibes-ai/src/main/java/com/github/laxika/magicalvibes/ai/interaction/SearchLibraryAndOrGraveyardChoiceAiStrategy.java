package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;

/** Chooses the highest-value card from a combined library and graveyard search pool. */
@Slf4j
class SearchLibraryAndOrGraveyardChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.SearchLibraryAndOrGraveyardChoice> {

    @Override
    public Class<PendingInteraction.SearchLibraryAndOrGraveyardChoice> handledType() {
        return PendingInteraction.SearchLibraryAndOrGraveyardChoice.class;
    }

    @Override
    public void answer(PendingInteraction.SearchLibraryAndOrGraveyardChoice interaction,
                       AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }

        List<Card> pool = interaction.pool();
        if (pool.isEmpty()) {
            ctx.gameActions().answerInteraction(new InteractionAnswer.CardsChosen(List.of()));
            return;
        }

        List<Card> chosen = pool.stream()
                .sorted(Comparator.comparingInt(this::score).reversed())
                .limit(interaction.maxCount())
                .toList();
        log.info("AI: Choosing {} card(s) from library or graveyard in game {}", chosen.size(), ctx.gameId());
        ctx.gameActions().answerInteraction(new InteractionAnswer.CardsChosen(
                chosen.stream().map(Card::getId).toList()));
    }

    private int score(Card card) {
        return card.hasType(CardType.LAND) ? card.getManaValue() : card.getManaValue() * 2 + 10;
    }
}
