package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

/** Prefers expensive offered spells while respecting the two-spell, six-mana-value limit. */
class InvokeCalamityCastChoiceAiStrategy implements AiInteractionStrategy<PendingInteraction.InvokeCalamityCastChoice> {

    @Override
    public Class<PendingInteraction.InvokeCalamityCastChoice> handledType() {
        return PendingInteraction.InvokeCalamityCastChoice.class;
    }

    @Override
    public void answer(PendingInteraction.InvokeCalamityCastChoice interaction, AiInteractionContext ctx)
            throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }
        List<Card> candidates = Stream.concat(
                        ctx.gameData().playerHands.getOrDefault(interaction.playerId(), List.of()).stream(),
                        ctx.gameData().playerGraveyards.getOrDefault(interaction.playerId(), List.of()).stream())
                .filter(card -> interaction.validCardIds().contains(card.getId()))
                .sorted(Comparator.comparingInt(Card::getManaValue).reversed())
                .toList();
        List<UUID> selected = new ArrayList<>();
        int remainingManaValue = 6;
        for (Card card : candidates) {
            if (selected.size() == 2) {
                break;
            }
            if (card.getManaValue() <= remainingManaValue && !selected.contains(card.getId())) {
                selected.add(card.getId());
                remainingManaValue -= card.getManaValue();
            }
        }
        ctx.gameActions().answerInteraction(new InteractionAnswer.CardsChosen(selected));
    }
}
