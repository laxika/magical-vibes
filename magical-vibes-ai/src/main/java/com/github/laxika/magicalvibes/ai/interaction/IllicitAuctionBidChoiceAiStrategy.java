package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.networking.message.XValueChosenRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Answers an Illicit Auction life bid: the AI passes (bids 0, which never tops the high bid) rather
 * than losing life to raise. As the caster it opens the bidding at 0 and simply keeps the creature
 * when opponents also pass; as a non-caster it declines to pay life to fight over the creature.
 */
@Slf4j
class IllicitAuctionBidChoiceAiStrategy implements AiInteractionStrategy<PendingInteraction.IllicitAuctionBidChoice> {

    @Override
    public Class<PendingInteraction.IllicitAuctionBidChoice> handledType() {
        return PendingInteraction.IllicitAuctionBidChoice.class;
    }

    @Override
    public void answer(PendingInteraction.IllicitAuctionBidChoice interaction, AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }

        log.info("AI: Passing on Illicit Auction bid for {} in game {}", interaction.cardName(), ctx.gameId());
        ctx.gameActions().handleXValueChosen(ctx.selfConnection(), new XValueChosenRequest(null, 0));
    }
}
