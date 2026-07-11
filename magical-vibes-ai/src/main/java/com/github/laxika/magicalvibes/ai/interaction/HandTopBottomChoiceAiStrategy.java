package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.networking.message.HandTopBottomChosenRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Answers hand/top/bottom choices: prefers a nonland for the hand, and among two spells
 * takes the more expensive one.
 */
@Slf4j
class HandTopBottomChoiceAiStrategy implements AiInteractionStrategy<PendingInteraction.HandTopBottomChoice> {

    @Override
    public Class<PendingInteraction.HandTopBottomChoice> handledType() {
        return PendingInteraction.HandTopBottomChoice.class;
    }

    @Override
    public void answer(PendingInteraction.HandTopBottomChoice interaction, AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }

        List<Card> cards = interaction.cards();
        if (cards == null || cards.size() < 2) {
            return;
        }

        int handCardIndex = 0;
        int topCardIndex = 1;

        Card card0 = cards.get(0);
        Card card1 = cards.get(1);

        boolean card0IsLand = card0.hasType(CardType.LAND);
        boolean card1IsLand = card1.hasType(CardType.LAND);

        if (card0IsLand && !card1IsLand) {
            handCardIndex = 1;
            topCardIndex = 0;
        } else if (!card0IsLand && card1IsLand) {
            handCardIndex = 0;
            topCardIndex = 1;
        } else {
            if (card1.getManaValue() > card0.getManaValue()) {
                handCardIndex = 1;
                topCardIndex = 0;
            }
        }

        log.info("AI: Choosing hand={} top={} in game {}", handCardIndex, topCardIndex, ctx.gameId());
        ctx.gameActions().handleHandTopBottomChosen(ctx.selfConnection(),
                new HandTopBottomChosenRequest(handCardIndex, topCardIndex));
    }
}
