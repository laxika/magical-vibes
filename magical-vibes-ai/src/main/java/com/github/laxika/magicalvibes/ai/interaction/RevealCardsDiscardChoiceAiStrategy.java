package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.networking.message.CardChosenRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Answers the Blackmail flow. As the revealing target player, the AI reveals its lowest-mana-value
 * cards (giving up the least). As the controller, it makes the target discard the highest-mana-value
 * revealed card.
 */
@Slf4j
class RevealCardsDiscardChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.RevealCardsDiscardChoice> {

    @Override
    public Class<PendingInteraction.RevealCardsDiscardChoice> handledType() {
        return PendingInteraction.RevealCardsDiscardChoice.class;
    }

    @Override
    public void answer(PendingInteraction.RevealCardsDiscardChoice interaction, AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.decidingPlayerId())) {
            return;
        }

        List<Card> targetHand = ctx.gameData().playerHands.get(interaction.targetPlayerId());
        List<Integer> validIndices = interaction.validIndices();
        if (targetHand == null || validIndices == null || validIndices.isEmpty()) {
            return;
        }

        int chosenIndex;
        if (interaction.revealStage()) {
            // Reveal the lowest-value card (indices are into the target's own hand).
            chosenIndex = validIndices.stream()
                    .min(Comparator.comparingInt(i -> targetHand.get(i).getManaValue()))
                    .orElse(validIndices.iterator().next());
        } else {
            // Discard the highest-value revealed card (indices are into the revealed set).
            List<UUID> revealed = interaction.revealedCardIds();
            chosenIndex = validIndices.stream()
                    .max(Comparator.comparingInt(i -> manaValueOf(targetHand, revealed.get(i))))
                    .orElse(validIndices.iterator().next());
        }

        log.info("AI: Choosing card {} in reveal-and-discard in game {}", chosenIndex, ctx.gameId());
        ctx.gameActions().handleCardChosen(ctx.selfConnection(), new CardChosenRequest(chosenIndex));
    }

    private static int manaValueOf(List<Card> hand, UUID cardId) {
        return hand.stream().filter(c -> c.getId().equals(cardId)).findFirst()
                .map(Card::getManaValue).orElse(0);
    }
}
