package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/** Chooses the highest-mana-value valid card from the targeted player's hand or graveyard. */
@Slf4j
class ExileNonlandCardFromTargetHandOrGraveyardChoiceAiStrategy implements
        AiInteractionStrategy<PendingInteraction.ExileNonlandCardFromTargetHandOrGraveyardChoice> {

    @Override
    public Class<PendingInteraction.ExileNonlandCardFromTargetHandOrGraveyardChoice> handledType() {
        return PendingInteraction.ExileNonlandCardFromTargetHandOrGraveyardChoice.class;
    }

    @Override
    public void answer(PendingInteraction.ExileNonlandCardFromTargetHandOrGraveyardChoice interaction,
                       AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId()) || interaction.validCardIds().isEmpty()) {
            return;
        }

        List<Card> hand = ctx.gameData().playerHands.getOrDefault(interaction.targetPlayerId(), List.of());
        List<Card> graveyard = ctx.gameData().playerGraveyards.getOrDefault(interaction.targetPlayerId(), List.of());
        UUID chosenCardId = interaction.validCardIds().stream()
                .max(Comparator.comparingInt(cardId -> manaValue(cardId, hand, graveyard)))
                .orElseThrow();

        log.info("AI: Exiling card {} from an opponent's hand or graveyard in game {}",
                chosenCardId, ctx.gameId());
        ctx.gameActions().answerInteraction(new InteractionAnswer.CardsChosen(List.of(chosenCardId)));
    }

    private static int manaValue(UUID cardId, List<Card> hand, List<Card> graveyard) {
        return java.util.stream.Stream.concat(hand.stream(), graveyard.stream())
                .filter(card -> card.getId().equals(cardId))
                .mapToInt(Card::getManaValue)
                .findFirst()
                .orElse(0);
    }
}
