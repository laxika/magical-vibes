package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingOpponentChoosesCardToHandRestToGraveyard;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Handles Intuition and Burning-Rune Demon's searches: the controller picks exactly the required
 * number of library cards and reveals them, then the opponent is prompted to choose which of them
 * goes into the controller's hand ({@link PendingOpponentChoosesCardToHandRestToGraveyard} routes
 * that answer). The picked cards leave the library here; the shuffle happens once the opponent has
 * chosen.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IntuitionSearchChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.IntuitionSearchChoice> {

    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<PendingInteraction.IntuitionSearchChoice> handledType() {
        return PendingInteraction.IntuitionSearchChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player, PendingInteraction.IntuitionSearchChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        List<UUID> cardIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (cardIds == null) {
            cardIds = List.of();
        }
        if (cardIds.size() != interaction.count()) {
            throw new IllegalStateException("Must choose exactly " + interaction.count() + " cards");
        }

        List<UUID> validIds = interaction.validCardIds();
        Set<UUID> chosenIds = new HashSet<>();
        Set<String> chosenNames = new HashSet<>();
        for (UUID id : cardIds) {
            if (!validIds.contains(id)) {
                throw new IllegalStateException("Invalid card ID: " + id);
            }
            if (!chosenIds.add(id)) {
                throw new IllegalStateException("Duplicate card ID: " + id);
            }
            if (interaction.requireDifferentNames()) {
                Card card = interaction.pool().stream()
                        .filter(candidate -> candidate.getId().equals(id))
                        .findFirst()
                        .orElseThrow();
                if (!chosenNames.add(card.getName())) {
                    throw new IllegalStateException("Must choose cards with different names");
                }
            }
        }

        UUID controllerId = interaction.playerId();
        String controllerName = gameData.playerIdToName.get(controllerId);

        List<Card> chosen = new ArrayList<>();
        for (Card card : interaction.pool()) {
            if (chosenIds.contains(card.getId())) {
                chosen.add(card);
            }
        }

        gameData.interaction.clearAwaitingInput();

        List<Card> deck = gameData.playerDecks.get(controllerId);
        deck.removeIf(card -> chosenIds.contains(card.getId()));

        GameLog.Builder revealBuilder = GameLog.builder().text(controllerName + " reveals ");
        for (int i = 0; i < chosen.size(); i++) {
            if (i > 0) revealBuilder.text(", ");
            revealBuilder.card(chosen.get(i));
        }
        revealBuilder.text(".");
        gameLogService.append(gameData, revealBuilder.build());

        gameData.queueInteraction(new PendingOpponentChoosesCardToHandRestToGraveyard(controllerId));
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryRevealChoice(
                interaction.opponentId(), chosen, chosen.stream().map(Card::getId).toList(),
                false, true, false, false, false, 0, null, 1,
                "Choose a card to put into " + controllerName
                        + "'s hand. The rest go into their graveyard.", 1, false));

        log.info("Game {} - library search: {} revealed {} cards, opponent must choose",
                gameData.id, controllerName, chosen.size());
    }
}
