package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Keeps the highest-mana-value legal cards during a keep-cards-in-hand interaction. */
class KeepCardsInHandChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.KeepCardsInHandChoice> {

    @Override
    public Class<PendingInteraction.KeepCardsInHandChoice> handledType() {
        return PendingInteraction.KeepCardsInHandChoice.class;
    }

    @Override
    public void answer(
            PendingInteraction.KeepCardsInHandChoice interaction,
            AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }
        Set<UUID> legalIds = Set.copyOf(interaction.validCardIds());
        List<UUID> kept = ctx.gameData().playerHands
                .getOrDefault(interaction.playerId(), List.of())
                .stream()
                .filter(card -> legalIds.contains(card.getId()))
                .sorted(Comparator.comparingInt(Card::getManaValue).reversed())
                .limit(interaction.maxCount())
                .map(Card::getId)
                .toList();
        ctx.gameActions().answerInteraction(
                new InteractionAnswer.CardsChosen(kept));
    }
}
