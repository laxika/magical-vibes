package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.ChooseMultipleCardsMessage;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.ThievesAuctionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Prompts the current Thieves' Auction chooser to pick one card from the shared exiled pool
 * ({@link ChooseMultipleCardsMessage} with {@code maxCount == 1}). The pick is applied by
 * {@link ThievesAuctionService#applyPick}, which advances the auction to the next player.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ThievesAuctionChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.ThievesAuctionChoice> {

    private final SessionManager sessionManager;
    private final CardViewFactory cardViewFactory;
    private final ThievesAuctionService thievesAuctionService;

    @Override
    public Class<PendingInteraction.ThievesAuctionChoice> handledType() {
        return PendingInteraction.ThievesAuctionChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public UUID decidingPlayerId(PendingInteraction.ThievesAuctionChoice interaction) {
        return interaction.choosingPlayerId();
    }

    @Override
    public void prompt(GameData gameData, PendingInteraction.ThievesAuctionChoice interaction, UUID recipientId) {
        List<CardView> cardViews = interaction.pool().stream()
                .map(cardViewFactory::create)
                .toList();
        List<UUID> validCardIds = interaction.pool().stream().map(Card::getId).toList();

        sessionManager.sendToPlayer(recipientId,
                new ChooseMultipleCardsMessage(validCardIds, cardViews, 1, interaction.prompt()));

        String playerName = gameData.playerIdToName.get(interaction.choosingPlayerId());
        log.info("Game {} - Awaiting {} to choose an auctioned card ({} remaining)",
                gameData.id, playerName, interaction.pool().size());
    }

    @Override
    public void handleAnswer(GameData gameData, Player player, PendingInteraction.ThievesAuctionChoice interaction,
                             InteractionAnswer answer) {
        List<UUID> cardIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        thievesAuctionService.applyPick(gameData, player, cardIds);
    }
}
