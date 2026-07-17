package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.ChooseFromListMessage;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.turn.TurnProgressionService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Prompts the "top or bottom" pick for the cards the player already chose to return from hand,
 * then moves all of them from hand to that end of the library (Dream Cache). The chosen cards keep
 * their selection order (first chosen ends up nearest the top when placed on top).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PutCardsFromHandOnLibraryDestinationChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.PutCardsFromHandOnLibraryDestinationChoice> {

    // The option strings live on the record so its legalOptions() stays in sync with the prompt.
    private static final String TOP = PendingInteraction.PutCardsFromHandOnLibraryDestinationChoice.OPTIONS.get(0);

    private final SessionManager sessionManager;
    private final GameBroadcastService gameBroadcastService;
    private final TurnProgressionService turnProgressionService;

    @Override
    public Class<PendingInteraction.PutCardsFromHandOnLibraryDestinationChoice> handledType() {
        return PendingInteraction.PutCardsFromHandOnLibraryDestinationChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.ListChoiceMade.class;
    }

    @Override
    public void prompt(GameData gameData, PendingInteraction.PutCardsFromHandOnLibraryDestinationChoice interaction,
                       UUID recipientId) {
        sessionManager.sendToPlayer(recipientId, new ChooseFromListMessage(
                PendingInteraction.PutCardsFromHandOnLibraryDestinationChoice.OPTIONS,
                "Put the chosen cards on the top or bottom of your library?"));
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.PutCardsFromHandOnLibraryDestinationChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your choice to make");
        }
        boolean onTop = TOP.equalsIgnoreCase(((InteractionAnswer.ListChoiceMade) answer).choice());

        UUID playerId = player.getId();
        List<Card> hand = gameData.playerHands.get(playerId);
        List<Card> deck = gameData.playerDecks.get(playerId);

        List<Card> moving = new ArrayList<>();
        for (UUID id : interaction.chosenCardIds()) {
            Card found = hand.stream().filter(c -> c.getId().equals(id)).findFirst().orElse(null);
            if (found != null) {
                hand.remove(found);
                moving.add(found);
            }
        }

        if (onTop) {
            // Place so the first chosen card ends up nearest the top of the library.
            for (int i = moving.size() - 1; i >= 0; i--) {
                deck.add(0, moving.get(i));
            }
        } else {
            deck.addAll(moving);
        }

        gameData.interaction.clearAwaitingInput();

        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(player.getUsername() + " puts " + moving.size()
                + " card(s) on the " + (onTop ? "top" : "bottom") + " of their library."));
        log.info("Game {} - {} put {} card(s) on {} of library", gameData.id, player.getUsername(),
                moving.size(), onTop ? "top" : "bottom");

        turnProgressionService.resolveAutoPass(gameData);
    }
}
