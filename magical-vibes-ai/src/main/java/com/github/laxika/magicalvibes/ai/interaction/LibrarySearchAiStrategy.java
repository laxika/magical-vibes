package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.networking.message.LibraryCardChosenRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Answers library search picks: the AI takes the highest-value nonland (or the first card).
 * Ported verbatim from the legacy {@code AiChoiceHandler.handleLibrarySearch} heuristic.
 */
@Slf4j
class LibrarySearchAiStrategy implements AiInteractionStrategy<PendingInteraction.LibrarySearch> {

    @Override
    public Class<PendingInteraction.LibrarySearch> handledType() {
        return PendingInteraction.LibrarySearch.class;
    }

    @Override
    public void answer(PendingInteraction.LibrarySearch interaction, AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.params().playerId())) {
            return;
        }

        List<Card> searchCards = interaction.params().cards();
        if (searchCards == null || searchCards.isEmpty()) {
            return;
        }

        // Pick highest value non-land, or first card
        int bestIndex = 0;
        int bestScore = -1;
        for (int i = 0; i < searchCards.size(); i++) {
            Card card = searchCards.get(i);
            int score = card.hasType(CardType.LAND) ? card.getManaValue() : card.getManaValue() * 2 + 10;
            if (score > bestScore) {
                bestScore = score;
                bestIndex = i;
            }
        }

        log.info("AI: Choosing card {} from library in game {}", searchCards.get(bestIndex).getName(), ctx.gameId());
        ctx.gameActions().handleLibraryCardChosen(ctx.selfConnection(), new LibraryCardChosenRequest(bestIndex));
    }
}
