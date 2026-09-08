package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

/** Chooses the highest-mana-value eligible card available from the AI's sideboard or exile. */
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
        Stream<Card> sideboardCards = ctx.gameData().playerSideboards
                .getOrDefault(ctx.aiPlayerId(), List.of()).stream();
        Stream<Card> exiledCards;
        synchronized (ctx.gameData().exiledCards) {
            exiledCards = ctx.gameData().exiledCards.stream()
                    .filter(entry -> ctx.aiPlayerId().equals(entry.ownerId()) && !entry.faceDown())
                    .map(ExiledCardEntry::card)
                    .toList()
                    .stream();
        }
        List<UUID> chosen = Stream.concat(sideboardCards, exiledCards)
                .filter(card -> validIds.contains(card.getId()))
                .max(Comparator.comparingInt(Card::getManaValue))
                .map(card -> List.of(card.getId()))
                .orElseGet(List::of);

        ctx.gameActions().answerInteraction(new InteractionAnswer.CardsChosen(chosen));
    }
}
