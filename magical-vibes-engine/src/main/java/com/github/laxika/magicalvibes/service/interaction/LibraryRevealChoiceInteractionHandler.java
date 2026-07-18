package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.InteractionPromptMessage;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.input.LibraryChoiceHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Handles "select zero or more of the revealed/looked-at library cards" choices. Card views
 * derive from the record's begin-time ordered valid IDs against the held-out cards; a null
 * prompt means the begin site sends its own choice message and none is sent on replay either.
 * The answer (battlefield/hand placement, punisher life payment, Karn flows) is applied by
 * {@link LibraryChoiceHandlerService#handleLibraryRevealChoice}.
 */
@Component
@RequiredArgsConstructor
public class LibraryRevealChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.LibraryRevealChoice> {

    private final SessionManager sessionManager;
    private final CardViewFactory cardViewFactory;
    private final LibraryChoiceHandlerService libraryChoiceHandlerService;

    @Override
    public Class<PendingInteraction.LibraryRevealChoice> handledType() {
        return PendingInteraction.LibraryRevealChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void prompt(GameData gameData, PendingInteraction.LibraryRevealChoice interaction, UUID recipientId) {
        if (interaction.prompt() == null) {
            return;
        }
        Map<UUID, Card> cardsById = interaction.allCards().stream()
                .collect(Collectors.toMap(Card::getId, Function.identity(), (a, b) -> a));
        List<CardView> cardViews = interaction.validCardIds().stream()
                .map(cardsById::get)
                .map(cardViewFactory::create)
                .toList();
        sessionManager.sendToPlayer(recipientId, InteractionPromptMessage.multiCardPick(
                new ArrayList<>(interaction.validCardIds()), cardViews, interaction.maxCount(),
                interaction.prompt()));
    }

    @Override
    public void handleAnswer(GameData gameData, Player player, PendingInteraction.LibraryRevealChoice interaction,
                             InteractionAnswer answer) {
        List<UUID> cardIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        libraryChoiceHandlerService.handleLibraryRevealChoice(gameData, player, cardIds);
    }
}
