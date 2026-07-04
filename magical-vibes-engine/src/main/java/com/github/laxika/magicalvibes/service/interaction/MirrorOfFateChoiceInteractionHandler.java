package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.ChooseMultipleCardsMessage;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Handles the Mirror of Fate choice: pick up to seven face-up exiled cards to put on top of
 * the library (the rest of the library is exiled). Card views are re-derived from the
 * player's exile zone at prompt time; the answer is applied by
 * {@link ExileSupport#handleMirrorOfFateChoice}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MirrorOfFateChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.MirrorOfFateChoice> {

    private final SessionManager sessionManager;
    private final CardViewFactory cardViewFactory;
    private final ExileSupport exileSupport;

    @Override
    public Class<PendingInteraction.MirrorOfFateChoice> handledType() {
        return PendingInteraction.MirrorOfFateChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public UUID decidingPlayerId(PendingInteraction.MirrorOfFateChoice interaction) {
        return interaction.playerId();
    }

    @Override
    public void prompt(GameData gameData, PendingInteraction.MirrorOfFateChoice interaction, UUID recipientId) {
        List<Card> exiledCards = gameData.getPlayerExiledCards(interaction.playerId());
        List<CardView> cardViews = exiledCards.stream()
                .filter(c -> interaction.validCardIds().contains(c.getId()))
                .map(cardViewFactory::create)
                .toList();

        sessionManager.sendToPlayer(recipientId,
                new ChooseMultipleCardsMessage(new ArrayList<>(interaction.validCardIds()), cardViews,
                        interaction.maxCount(),
                        "Choose up to seven face-up exiled cards you own to put on top of your library."));

        String playerName = gameData.playerIdToName.get(interaction.playerId());
        log.info("Game {} - Awaiting {} to choose exiled cards for Mirror of Fate (up to {})",
                gameData.id, playerName, interaction.maxCount());
    }

    @Override
    public void handleAnswer(GameData gameData, Player player, PendingInteraction.MirrorOfFateChoice interaction,
                             InteractionAnswer answer) {
        List<UUID> cardIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        exileSupport.handleMirrorOfFateChoice(gameData, player, cardIds);
    }
}
