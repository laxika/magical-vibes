package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.InteractionPromptMessage;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.input.GraveyardChoiceHandlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Handles "select zero or more cards" choices over a graveyard-sourced card list
 * (graveyard-targeting spells and triggers, plus pile separation over just-exiled cards).
 * IDs and card views are derived from the record's begin-time card list; the answer is
 * applied by {@link GraveyardChoiceHandlerService#handleMultipleCardsChosen}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MultiGraveyardChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.MultiGraveyardChoice> {

    private final SessionManager sessionManager;
    private final CardViewFactory cardViewFactory;
    private final GraveyardChoiceHandlerService graveyardChoiceHandlerService;

    @Override
    public Class<PendingInteraction.MultiGraveyardChoice> handledType() {
        return PendingInteraction.MultiGraveyardChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void prompt(GameData gameData, PendingInteraction.MultiGraveyardChoice interaction, UUID recipientId) {
        List<UUID> validCardIds = interaction.validCardIds();
        List<CardView> cardViews = interaction.cards().stream().map(cardViewFactory::create).toList();

        sessionManager.sendToPlayer(recipientId, InteractionPromptMessage.multiCardPick(
                validCardIds, cardViews, interaction.maxCount(), interaction.prompt()));

        String playerName = gameData.playerIdToName.get(interaction.playerId());
        log.info("Game {} - Awaiting {} to choose up to {} cards from graveyards",
                gameData.id, playerName, interaction.maxCount());
    }

    @Override
    public void handleAnswer(GameData gameData, Player player, PendingInteraction.MultiGraveyardChoice interaction,
                             InteractionAnswer answer) {
        List<UUID> cardIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        graveyardChoiceHandlerService.handleMultipleCardsChosen(gameData, player, cardIds);
    }
}
