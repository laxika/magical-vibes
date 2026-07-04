package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.ChooseCardFromLibraryMessage;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.input.LibraryChoiceHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Handles search-style single-card picks from a presented library subset (tutors,
 * look-at-top-N picks, Head Games, Sphinx Ambassador, …). Card views derive from the
 * carried {@code params.cards()}; the message prompt and fail-to-find flag are the exact
 * begin-time values. The answer (destination handling, countdown, reorders, Sphinx flow)
 * is applied by {@link LibraryChoiceHandlerService#handleLibraryCardChosen}.
 */
@Component
@RequiredArgsConstructor
public class LibrarySearchInteractionHandler
        implements InteractionHandler<PendingInteraction.LibrarySearch> {

    private final SessionManager sessionManager;
    private final CardViewFactory cardViewFactory;
    private final LibraryChoiceHandlerService libraryChoiceHandlerService;

    @Override
    public Class<PendingInteraction.LibrarySearch> handledType() {
        return PendingInteraction.LibrarySearch.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.LibraryCardChosen.class;
    }

    @Override
    public UUID decidingPlayerId(PendingInteraction.LibrarySearch interaction) {
        return interaction.params().playerId();
    }

    @Override
    public void prompt(GameData gameData, PendingInteraction.LibrarySearch interaction, UUID recipientId) {
        List<CardView> cardViews = interaction.params().cards().stream().map(cardViewFactory::create).toList();
        sessionManager.sendToPlayer(recipientId, new ChooseCardFromLibraryMessage(
                cardViews, interaction.messagePrompt(), interaction.messageCanFailToFind()));
    }

    @Override
    public void handleAnswer(GameData gameData, Player player, PendingInteraction.LibrarySearch interaction,
                             InteractionAnswer answer) {
        int cardIndex = ((InteractionAnswer.LibraryCardChosen) answer).cardIndex();
        libraryChoiceHandlerService.handleLibraryCardChosen(gameData, player, cardIndex);
    }
}
