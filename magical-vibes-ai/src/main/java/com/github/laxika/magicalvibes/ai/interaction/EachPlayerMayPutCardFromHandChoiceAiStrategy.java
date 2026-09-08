package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/** Chooses the highest-mana-value eligible card for a simultaneous put-onto-battlefield effect. */
class EachPlayerMayPutCardFromHandChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.EachPlayerMayPutCardFromHandChoice> {

    @Override
    public Class<PendingInteraction.EachPlayerMayPutCardFromHandChoice> handledType() {
        return PendingInteraction.EachPlayerMayPutCardFromHandChoice.class;
    }

    @Override
    public void answer(PendingInteraction.EachPlayerMayPutCardFromHandChoice interaction,
                       AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }

        List<Card> hand = ctx.gameData().playerHands.getOrDefault(interaction.playerId(), List.of());
        UUID choice = hand.stream()
                .filter(card -> interaction.validCardIds().contains(card.getId()))
                .max(Comparator.comparingInt(Card::getManaValue))
                .map(Card::getId)
                .orElse(null);
        ctx.gameActions().answerInteraction(new InteractionAnswer.CardsChosen(
                choice == null ? List.of() : List.of(choice)));
    }
}
