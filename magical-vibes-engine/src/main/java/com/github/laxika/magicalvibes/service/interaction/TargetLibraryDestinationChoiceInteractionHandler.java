package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Resolves a target owner's choice between the top and bottom of their library.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TargetLibraryDestinationChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.TargetLibraryDestinationChoice> {

    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<PendingInteraction.TargetLibraryDestinationChoice> handledType() {
        return PendingInteraction.TargetLibraryDestinationChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.ListChoiceMade.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.TargetLibraryDestinationChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your choice to make");
        }
        boolean onFirstChoice = interaction.firstOption().equalsIgnoreCase(
                ((InteractionAnswer.ListChoiceMade) answer).choice());

        if (!onFirstChoice) {
            List<Card> deck = gameData.playerDecks.get(interaction.playerId());
            Card card = deck == null ? null
                    : deck.stream().filter(c -> c.getId().equals(interaction.cardId())).findFirst().orElse(null);
            if (card != null) {
                deck.remove(card);
                deck.add(card);
            }
        }

        gameData.interaction.clearAwaitingInput();
        gameLogService.append(gameData, GameLog.text(interaction.cardName() + " is put on the "
                + (onFirstChoice ? interaction.firstOption().toLowerCase() : "bottom")
                + " of its owner's library."));
        log.info("Game {} - {} put on {} of its owner's library", gameData.id,
                interaction.cardName(), onFirstChoice ? interaction.firstOption().toLowerCase() : "bottom");

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
