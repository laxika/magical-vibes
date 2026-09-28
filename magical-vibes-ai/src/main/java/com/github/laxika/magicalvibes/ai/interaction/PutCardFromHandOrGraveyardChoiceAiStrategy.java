package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

/** Chooses the highest-mana-value eligible card from the interaction's eligible zones. */
class PutCardFromHandOrGraveyardChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.PutCardFromHandOrGraveyardChoice> {

    @Override
    public Class<PendingInteraction.PutCardFromHandOrGraveyardChoice> handledType() {
        return PendingInteraction.PutCardFromHandOrGraveyardChoice.class;
    }

    @Override
    public void answer(PendingInteraction.PutCardFromHandOrGraveyardChoice interaction,
                       AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }

        List<UUID> validIds = interaction.validCardIds();
        Stream<Card> eligibleCards = ctx.gameData().playerHands
                .getOrDefault(ctx.aiPlayerId(), List.of()).stream();
        if (interaction.includeGraveyard()) {
            eligibleCards = Stream.concat(eligibleCards,
                    ctx.gameData().playerGraveyards.getOrDefault(ctx.aiPlayerId(), List.of()).stream());
        }
        if (interaction.includeCommandZone()) {
            eligibleCards = Stream.concat(eligibleCards,
                    ctx.gameData().playerCommandZones.getOrDefault(ctx.aiPlayerId(), List.of()).stream());
        }
        UUID choice = eligibleCards
                .filter(card -> validIds.contains(card.getId()))
                .max(Comparator.comparingInt(Card::getManaValue))
                .map(Card::getId)
                .orElse(validIds.isEmpty() ? null : validIds.getFirst());
        ctx.gameActions().answerInteraction(new InteractionAnswer.CardsChosen(
                choice == null ? List.of() : List.of(choice)));
    }
}
