package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Validates and resolves Guided Passage's opponent-selected library cards. */
@Component
@RequiredArgsConstructor
public class GuidedPassageChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.GuidedPassageChoice> {

    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<PendingInteraction.GuidedPassageChoice> handledType() {
        return PendingInteraction.GuidedPassageChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
            PendingInteraction.GuidedPassageChoice interaction, InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        List<UUID> cardIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (cardIds == null) {
            cardIds = List.of();
        }
        if (cardIds.size() > 3) {
            throw new IllegalStateException("Cannot choose more than three cards");
        }

        Set<UUID> validIds = new HashSet<>(interaction.validCardIds());
        Set<UUID> chosenIds = new HashSet<>();
        for (UUID cardId : cardIds) {
            if (!validIds.contains(cardId)) {
                throw new IllegalStateException("Invalid card ID: " + cardId);
            }
            if (!chosenIds.add(cardId)) {
                throw new IllegalStateException("Duplicate card ID: " + cardId);
            }
        }

        List<Card> library = gameData.playerDecks.get(interaction.controllerId());
        Set<UUID> libraryIds = library == null ? Set.of()
                : library.stream().map(Card::getId).collect(Collectors.toSet());
        if (library == null || !libraryIds.containsAll(chosenIds)) {
            throw new IllegalStateException("Guided Passage library is unavailable");
        }
        List<Card> selected = interaction.pool().stream()
                .filter(card -> chosenIds.contains(card.getId()))
                .toList();

        requireOnePerAvailableCategory(interaction.pool(), selected, CardType.CREATURE,
                "creature");
        requireOnePerAvailableCategory(interaction.pool(), selected, CardType.LAND, "land");
        requireOneNoncreatureNonlandPerAvailableCategory(interaction.pool(), selected);

        gameData.interaction.clearAwaitingInput();
        String controllerName = gameData.playerIdToName.get(interaction.controllerId());
        for (Card card : selected) {
            library.removeIf(libraryCard -> libraryCard.getId().equals(card.getId()));
            gameData.addCardToHand(interaction.controllerId(), card);
        }
        LibraryShuffleHelper.shuffleLibrary(gameData, interaction.controllerId());

        if (!selected.isEmpty()) {
            GameLog.Builder chosenBuilder = GameLog.builder()
                    .text(controllerName + " puts ");
            for (int i = 0; i < selected.size(); i++) {
                if (i > 0) {
                    chosenBuilder.text(", ");
                }
                chosenBuilder.card(selected.get(i));
            }
            chosenBuilder.text(" into their hand from Guided Passage.");
            gameLogService.append(gameData, chosenBuilder.build());
        }
        gameLogService.append(gameData, GameLog.text(controllerName + " shuffles their library."));
        inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private static void requireOnePerAvailableCategory(List<Card> pool, List<Card> selected,
            CardType type, String label) {
        long available = pool.stream().filter(card -> card.hasType(type)).count();
        long chosen = selected.stream().filter(card -> card.hasType(type)).count();
        if (chosen != (available == 0 ? 0 : 1)) {
            throw new IllegalStateException("Must choose exactly one " + label + " card when available");
        }
    }

    private static void requireOneNoncreatureNonlandPerAvailableCategory(List<Card> pool,
            List<Card> selected) {
        long available = pool.stream().filter(GuidedPassageChoiceInteractionHandler::isNoncreatureNonland).count();
        long chosen = selected.stream().filter(GuidedPassageChoiceInteractionHandler::isNoncreatureNonland).count();
        if (chosen != (available == 0 ? 0 : 1)) {
            throw new IllegalStateException(
                    "Must choose exactly one noncreature, nonland card when available");
        }
    }

    private static boolean isNoncreatureNonland(Card card) {
        return !card.hasType(CardType.CREATURE) && !card.hasType(CardType.LAND);
    }
}
