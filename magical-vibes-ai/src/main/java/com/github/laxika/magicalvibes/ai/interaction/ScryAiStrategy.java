package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.networking.message.ScryCompletedRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * Answers scry choices with the baseline heuristic: keep spells on top, put lands on the
 * bottom. (Hard AI overrides scry handling with a board-aware version at the decision-engine
 * level and only falls back to this strategy.)
 */
@Slf4j
class ScryAiStrategy implements AiInteractionStrategy<PendingInteraction.Scry> {

    @Override
    public Class<PendingInteraction.Scry> handledType() {
        return PendingInteraction.Scry.class;
    }

    @Override
    public void answer(PendingInteraction.Scry interaction, AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }

        List<Card> cards = interaction.cards();
        if (cards == null || cards.isEmpty()) {
            return;
        }

        // AI strategy: keep spells on top (sorted by mana value), put lands on bottom
        List<Integer> topOrder = new ArrayList<>();
        List<Integer> bottomOrder = new ArrayList<>();
        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);
            if (card.hasType(CardType.LAND)) {
                bottomOrder.add(i);
            } else {
                topOrder.add(i);
            }
        }

        log.info("AI: Scry {} - keeping {} on top, {} on bottom in game {}",
                cards.size(), topOrder.size(), bottomOrder.size(), ctx.gameId());
        ctx.gameActions().handleScryCompleted(ctx.selfConnection(), new ScryCompletedRequest(topOrder, bottomOrder));
    }
}
