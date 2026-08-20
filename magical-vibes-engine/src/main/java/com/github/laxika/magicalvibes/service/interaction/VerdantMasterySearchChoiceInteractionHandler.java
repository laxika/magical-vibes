package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/** Applies the controller's Verdant Mastery library-search choice. */
@Component
@RequiredArgsConstructor
public class VerdantMasterySearchChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.VerdantMasterySearchChoice> {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final InputCompletionService inputCompletionService;
    private final VerdantMasteryLandChoiceInteractionHandler landChoiceHandler;

    @Override
    public Class<PendingInteraction.VerdantMasterySearchChoice> handledType() {
        return PendingInteraction.VerdantMasterySearchChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.VerdantMasterySearchChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        List<UUID> cardIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (cardIds == null) {
            cardIds = List.of();
        }
        if (cardIds.size() > Math.min(4, interaction.pool().size())) {
            throw new IllegalStateException("Too many cards selected (max 4)");
        }

        Set<UUID> selectedIds = new HashSet<>();
        for (UUID cardId : cardIds) {
            if (!interaction.validCardIds().contains(cardId)) {
                throw new IllegalStateException("Invalid card ID: " + cardId);
            }
            if (!selectedIds.add(cardId)) {
                throw new IllegalStateException("Duplicate card ID: " + cardId);
            }
        }

        List<Card> selected = interaction.pool().stream()
                .filter(card -> selectedIds.contains(card.getId()))
                .toList();
        List<Card> deck = gameData.playerDecks.get(interaction.playerId());
        for (Card card : selected) {
            if (!deck.remove(card)) {
                throw new IllegalStateException("Selected card is no longer in the library");
            }
        }

        gameData.interaction.clearAwaitingInput();
        if (selected.isEmpty()) {
            LibraryShuffleHelper.shuffleLibrary(gameData, interaction.playerId());
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(interaction.playerId())
                            + " finds no basic land cards with Verdant Mastery. Library is shuffled."));
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        gameLogService.append(gameData, GameLog.text(
                gameData.playerIdToName.get(interaction.playerId()) + " reveals "
                        + selected.stream().map(Card::getName).collect(Collectors.joining(", "))
                        + " with Verdant Mastery."));

        UUID opponentId = interaction.alternateCost()
                ? gameQueryService.getOpponentId(gameData, interaction.playerId())
                : null;
        if (interaction.alternateCost() && opponentId != null) {
            landChoiceHandler.beginOpponentChoice(gameData, interaction.playerId(), opponentId, selected);
        } else if (selected.size() > 2) {
            landChoiceHandler.beginControllerChoice(gameData, interaction.playerId(), null, null, selected);
        } else {
            landChoiceHandler.resolveDistribution(gameData, interaction.playerId(), null, null, selected, List.of());
        }
    }
}
