package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.ChooseMultipleCardsMessage;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.effect.EffectResolutionService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import com.github.laxika.magicalvibes.service.turn.TurnProgressionService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Handles the Goblin Recruiter choice: from the held-out matching {@code pool}, the controller
 * picks any number of cards to reveal and put on top of their library. The unchosen matching cards
 * are returned to the library, the library is shuffled, and the chosen cards are placed on top
 * (ordered via {@link PendingInteraction.LibraryReorder} when more than one is kept).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SearchLibraryToTopChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.SearchLibraryToTopChoice> {

    private final SessionManager sessionManager;
    private final CardViewFactory cardViewFactory;
    private final GameBroadcastService gameBroadcastService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final TurnProgressionService turnProgressionService;
    private final EffectResolutionService effectResolutionService;

    @Override
    public Class<PendingInteraction.SearchLibraryToTopChoice> handledType() {
        return PendingInteraction.SearchLibraryToTopChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void prompt(GameData gameData, PendingInteraction.SearchLibraryToTopChoice interaction, UUID recipientId) {
        List<CardView> cardViews = interaction.pool().stream().map(cardViewFactory::create).toList();
        sessionManager.sendToPlayer(recipientId, new ChooseMultipleCardsMessage(
                new ArrayList<>(interaction.validCardIds()), cardViews, interaction.pool().size(),
                "Choose any number of " + interaction.subtypeLabel()
                        + " cards to reveal and put on top of your library."));
    }

    @Override
    public void handleAnswer(GameData gameData, Player player, PendingInteraction.SearchLibraryToTopChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        List<UUID> cardIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (cardIds == null) {
            cardIds = List.of();
        }

        List<UUID> validIds = interaction.validCardIds();
        Set<UUID> uniqueIds = new HashSet<>();
        for (UUID id : cardIds) {
            if (!validIds.contains(id)) {
                throw new IllegalStateException("Invalid card ID: " + id);
            }
            if (!uniqueIds.add(id)) {
                throw new IllegalStateException("Duplicate card ID: " + id);
            }
        }

        UUID controllerId = interaction.playerId();
        String controllerName = gameData.playerIdToName.get(controllerId);

        // Split the held-out pool into the chosen cards (revealed, put on top) and the rest
        // (returned to the library); pool order is preserved for both.
        Set<UUID> chosenSet = new HashSet<>(cardIds);
        List<Card> chosen = new ArrayList<>();
        List<Card> rest = new ArrayList<>();
        for (Card card : interaction.pool()) {
            if (chosenSet.contains(card.getId())) {
                chosen.add(card);
            } else {
                rest.add(card);
            }
        }

        gameData.interaction.clearAwaitingInput();

        // Return the unchosen matching cards, then shuffle the whole library before placing the
        // chosen cards on top (CR: "reveal them, then shuffle and put those cards on top").
        List<Card> deck = gameData.playerDecks.get(controllerId);
        deck.addAll(rest);
        LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);

        if (!chosen.isEmpty()) {
            String revealed = chosen.stream().map(Card::getName).collect(Collectors.joining(", "));
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(controllerName + " reveals " + revealed
                    + " and shuffles their library."));
        }

        if (chosen.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(controllerName + " puts no cards on top of their library. Library is shuffled."));
            finishResolution(gameData);
        } else if (chosen.size() == 1) {
            deck.addFirst(chosen.getFirst());
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(controllerName + " puts "
                    + chosen.getFirst().getName() + " on top of their library."));
            finishResolution(gameData);
        } else {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(controllerName + " puts " + chosen.size()
                    + " cards on top of their library — choosing order."));
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryReorder(
                    controllerId, chosen, false, controllerId,
                    "Put these cards on top of your library in any order (top to bottom)."));
        }

        log.info("Game {} - Goblin Recruiter: {} put {} card(s) on top, shuffled {} back",
                gameData.id, controllerName, chosen.size(), rest.size());
    }

    /**
     * Finishes the resolution when no library-reorder step is needed (zero or one card kept):
     * resumes any remaining spell/ability effects and then auto-passes.
     */
    private void finishResolution(GameData gameData) {
        if (gameData.pendingEffectResolutionEntry != null) {
            effectResolutionService.resolveEffectsFrom(gameData,
                    gameData.pendingEffectResolutionEntry,
                    gameData.pendingEffectResolutionIndex);
        }
        if (!gameData.interaction.isAwaitingInput()) {
            turnProgressionService.resolveAutoPass(gameData);
        }
    }
}
