package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.networking.message.MultipleCardsChosenRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;

/**
 * Answers a Thieves' Auction pick: the AI grabs the highest-mana-value card remaining in the pool.
 */
@Slf4j
class ThievesAuctionChoiceAiStrategy implements AiInteractionStrategy<PendingInteraction.ThievesAuctionChoice> {

    @Override
    public Class<PendingInteraction.ThievesAuctionChoice> handledType() {
        return PendingInteraction.ThievesAuctionChoice.class;
    }

    @Override
    public void answer(PendingInteraction.ThievesAuctionChoice interaction, AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.choosingPlayerId())) {
            return;
        }

        Card pick = interaction.pool().stream()
                .max(Comparator.comparingInt(Card::getManaValue))
                .orElse(null);
        if (pick == null) {
            return;
        }

        log.info("AI: Picking {} in Thieves' Auction in game {}", pick.getName(), ctx.gameId());
        ctx.gameActions().handleMultipleCardsChosen(ctx.selfConnection(),
                new MultipleCardsChosenRequest(List.of(pick.getId())));
    }
}
