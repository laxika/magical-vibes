package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.effect.LibrarySearchCastPermission;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.IntStream;

/**
 * Answers library search picks by taking the highest-value actual search result. Cards presented
 * only as optional cast offers are skipped, and a qualified search legally fails to find when no
 * search result remains.
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

        LibrarySearchParams params = interaction.params();
        List<Card> searchCards = params.cards();
        if (searchCards == null || searchCards.isEmpty()) {
            if (params.canFailToFind()) {
                ctx.gameActions().answerInteraction(new InteractionAnswer.LibraryCardChosen(-1));
            }
            return;
        }

        List<Integer> searchResultIndices = IntStream.range(0, searchCards.size())
                .filter(index -> !isLibrarySearchCastOption(params, searchCards.get(index)))
                .boxed()
                .toList();
        if (searchResultIndices.isEmpty() && params.canFailToFind()) {
            log.info("AI: Declining library search with only optional cast offers in game {}", ctx.gameId());
            ctx.gameActions().answerInteraction(new InteractionAnswer.LibraryCardChosen(-1));
            return;
        }
        if (searchResultIndices.isEmpty()) {
            searchResultIndices = IntStream.range(0, searchCards.size()).boxed().toList();
        }

        int bestIndex = searchResultIndices.getFirst();
        int bestScore = -1;
        for (int i : searchResultIndices) {
            Card card = searchCards.get(i);
            int score = card.hasType(CardType.LAND) ? card.getManaValue() : card.getManaValue() * 2 + 10;
            if (score > bestScore) {
                bestScore = score;
                bestIndex = i;
            }
        }

        log.info("AI: Choosing card {} from library in game {}", searchCards.get(bestIndex).getName(), ctx.gameId());
        ctx.gameActions().answerInteraction(new InteractionAnswer.LibraryCardChosen(bestIndex));
    }

    private static boolean isLibrarySearchCastOption(LibrarySearchParams params, Card card) {
        return params.allowCastFromLibraryWhileSearching()
                && card.getEffects(EffectSlot.STATIC).stream()
                .anyMatch(LibrarySearchCastPermission.class::isInstance);
    }
}
