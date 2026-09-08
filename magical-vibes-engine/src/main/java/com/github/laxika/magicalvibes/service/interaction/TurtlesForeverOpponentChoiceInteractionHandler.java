package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Handles the opponent's choice of two Turtles Forever cards to put into the caster's hand. */
@Component
@RequiredArgsConstructor
public class TurtlesForeverOpponentChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.TurtlesForeverOpponentChoice> {

    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<PendingInteraction.TurtlesForeverOpponentChoice> handledType() {
        return PendingInteraction.TurtlesForeverOpponentChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.TurtlesForeverOpponentChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        List<UUID> cardIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (cardIds == null || cardIds.size() != 2) {
            throw new IllegalStateException("Must choose exactly 2 cards");
        }
        Set<UUID> chosenIds = new HashSet<>(cardIds);
        if (chosenIds.size() != 2 || !interaction.validCardIds().containsAll(chosenIds)) {
            throw new IllegalStateException("Invalid card selection");
        }

        List<Card> chosen = interaction.cards().stream()
                .filter(card -> chosenIds.contains(card.getId()))
                .toList();
        List<Card> rest = interaction.cards().stream()
                .filter(card -> !chosenIds.contains(card.getId()))
                .toList();

        gameData.interaction.clearAwaitingInput();
        for (Card card : chosen) {
            gameData.addCardToHand(interaction.controllerId(), card);
        }
        gameData.playerDecks.get(interaction.controllerId()).addAll(rest);
        LibraryShuffleHelper.shuffleLibrary(gameData, interaction.controllerId());
        gameLogService.append(gameData, GameLog.text(
                gameData.playerIdToName.get(interaction.controllerId())
                        + " puts two revealed cards into their hand and shuffles the rest into their library."));
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
