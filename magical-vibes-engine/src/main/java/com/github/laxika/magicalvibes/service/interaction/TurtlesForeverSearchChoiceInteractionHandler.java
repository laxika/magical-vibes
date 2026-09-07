package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Handles the controller's four-card Turtles Forever search selection. */
@Slf4j
@Component
@RequiredArgsConstructor
public class TurtlesForeverSearchChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.TurtlesForeverSearchChoice> {

    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<PendingInteraction.TurtlesForeverSearchChoice> handledType() {
        return PendingInteraction.TurtlesForeverSearchChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.TurtlesForeverSearchChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        List<UUID> cardIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (cardIds == null || cardIds.size() != 4) {
            throw new IllegalStateException("Must choose exactly 4 cards");
        }

        Map<UUID, Card> cardsById = new HashMap<>();
        for (Card card : interaction.pool()) {
            cardsById.put(card.getId(), card);
        }
        Set<UUID> chosenIds = new HashSet<>();
        Set<String> chosenNames = new HashSet<>();
        for (UUID cardId : cardIds) {
            Card card = cardsById.get(cardId);
            if (card == null) {
                throw new IllegalStateException("Invalid card ID: " + cardId);
            }
            if (!chosenIds.add(cardId)) {
                throw new IllegalStateException("Duplicate card ID: " + cardId);
            }
            if (!chosenNames.add(card.getName())) {
                throw new IllegalStateException("Must choose cards with different names");
            }
            validateSourceContainsCard(gameData, interaction, cardId);
        }

        List<Card> chosen = cardIds.stream().map(cardsById::get).toList();
        gameData.interaction.clearAwaitingInput();
        List<Card> deck = gameData.playerDecks.get(interaction.playerId());
        List<Card> sideboard = gameData.playerSideboards.get(interaction.playerId());
        deck.removeIf(card -> chosenIds.contains(card.getId()));
        if (sideboard != null) {
            sideboard.removeIf(card -> chosenIds.contains(card.getId()));
        }

        String controllerName = gameData.playerIdToName.get(interaction.playerId());
        GameLog.Builder revealBuilder = GameLog.builder().text(controllerName + " reveals ");
        for (int i = 0; i < chosen.size(); i++) {
            if (i > 0) revealBuilder.text(", ");
            revealBuilder.card(chosen.get(i));
        }
        revealBuilder.text(".");
        gameLogService.append(gameData, revealBuilder.build());

        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.TurtlesForeverOpponentChoice(
                        interaction.opponentId(), interaction.playerId(), new ArrayList<>(chosen)));
        log.info("Game {} - {} revealed four Turtles Forever cards; opponent chooses two",
                gameData.id, controllerName);
    }

    private void validateSourceContainsCard(GameData gameData,
                                            PendingInteraction.TurtlesForeverSearchChoice interaction,
                                            UUID cardId) {
        boolean librarySource = interaction.libraryCardIds().contains(cardId);
        boolean outsideGameSource = interaction.outsideGameCardIds().contains(cardId);
        if (librarySource == outsideGameSource) {
            throw new IllegalStateException("Invalid Turtles Forever card source: " + cardId);
        }
        List<Card> source = librarySource
                ? gameData.playerDecks.get(interaction.playerId())
                : gameData.playerSideboards.get(interaction.playerId());
        if (source == null || source.stream().noneMatch(card -> card.getId().equals(cardId))) {
            throw new IllegalStateException("Card is no longer available: " + cardId);
        }
    }
}
