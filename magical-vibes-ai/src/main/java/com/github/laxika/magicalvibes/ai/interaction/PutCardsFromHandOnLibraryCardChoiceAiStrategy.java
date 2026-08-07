package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Answers "put N cards from your hand on your library" picks (Dream Cache, Brainstorm's
 * put-back): returns the highest-mana-value cards, mirroring the baseline discard
 * heuristic — the most expensive cards are the ones the AI can least likely use soon.
 */
@Slf4j
class PutCardsFromHandOnLibraryCardChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.PutCardsFromHandOnLibraryCardChoice> {

    @Override
    public Class<PendingInteraction.PutCardsFromHandOnLibraryCardChoice> handledType() {
        return PendingInteraction.PutCardsFromHandOnLibraryCardChoice.class;
    }

    @Override
    public void answer(PendingInteraction.PutCardsFromHandOnLibraryCardChoice interaction,
                       AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }

        // Always answer, even with nothing to pick — an empty CardsChosen lets the engine
        // resolve the (then impossible) requirement instead of hanging the game.
        List<Card> cards = interaction.cards() != null ? interaction.cards() : List.of();

        // Scroll Rack's "any number" would otherwise let the baseline pick the whole hand and
        // reshuffle everything blind; one card keeps the swap a targeted upgrade.
        int limit = interaction.swapWithLibraryTop() ? 1 : Math.max(interaction.maxCount(), 0);

        List<UUID> chosen = cards.stream()
                .filter(c -> interaction.validCardIds().contains(c.getId()))
                .sorted(Comparator.comparingInt(Card::getManaValue).reversed())
                .limit(limit)
                .map(Card::getId)
                .toList();

        log.info("AI: Putting {} hand card(s) on the library in game {}", chosen.size(), ctx.gameId());
        ctx.gameActions().answerInteraction(new InteractionAnswer.CardsChosen(chosen));
    }
}
