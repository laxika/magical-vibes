package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves the "top or bottom of its owner's library" pick for a spell countered by Hinder. The
 * countered card was already put on top of its owner's library, so "Top" is a no-op and "Bottom"
 * moves it to the other end.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CounteredSpellLibraryDestinationChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.CounteredSpellLibraryDestinationChoice> {

    // The option strings live on the record so its legalOptions() stays in sync with the prompt.
    private static final String TOP = PendingInteraction.CounteredSpellLibraryDestinationChoice.OPTIONS.get(0);

    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<PendingInteraction.CounteredSpellLibraryDestinationChoice> handledType() {
        return PendingInteraction.CounteredSpellLibraryDestinationChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.ListChoiceMade.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.CounteredSpellLibraryDestinationChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your choice to make");
        }
        boolean onTop = TOP.equalsIgnoreCase(((InteractionAnswer.ListChoiceMade) answer).choice());

        if (!onTop) {
            List<Card> deck = gameData.playerDecks.get(interaction.ownerId());
            Card card = deck.stream().filter(c -> c.getId().equals(interaction.cardId())).findFirst().orElse(null);
            if (card != null) {
                deck.remove(card);
                deck.add(card);
            }
        }

        gameData.interaction.clearAwaitingInput();

        gameLogService.append(gameData, GameLog.text(interaction.cardName() + " is put on the "
                + (onTop ? "top" : "bottom") + " of its owner's library."));
        log.info("Game {} - countered {} put on {} of its owner's library", gameData.id,
                interaction.cardName(), onTop ? "top" : "bottom");

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
