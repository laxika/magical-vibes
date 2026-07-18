package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Answers library reorder choices: spells first (sorted by mana value ascending), lands last.
 */
@Slf4j
class LibraryReorderAiStrategy implements AiInteractionStrategy<PendingInteraction.LibraryReorder> {

    @Override
    public Class<PendingInteraction.LibraryReorder> handledType() {
        return PendingInteraction.LibraryReorder.class;
    }

    @Override
    public void answer(PendingInteraction.LibraryReorder interaction, AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }

        List<Card> cards = interaction.cards();
        if (cards == null || cards.isEmpty()) {
            return;
        }

        // Put spells on top, lands on bottom; sort by mana value ascending
        List<int[]> indexedCards = new ArrayList<>();
        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);
            int priority = card.hasType(CardType.LAND) ? 1000 + i : card.getManaValue();
            indexedCards.add(new int[]{i, priority});
        }
        indexedCards.sort(Comparator.comparingInt(a -> a[1]));

        List<Integer> order = indexedCards.stream().map(a -> a[0]).toList();

        log.info("AI: Reordering {} library cards in game {}", order.size(), ctx.gameId());
        ctx.gameActions().answerInteraction(ctx.selfConnection(), new InteractionAnswer.CardOrder(order));
    }
}
