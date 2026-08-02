package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MasterOfPredicamentsCardChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.MasterOfPredicamentsCardChoice> {

    private static final List<String> GUESS_OPTIONS = List.of("Greater than 4", "4 or less");

    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<PendingInteraction.MasterOfPredicamentsCardChoice> handledType() {
        return PendingInteraction.MasterOfPredicamentsCardChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardIndexChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.MasterOfPredicamentsCardChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        int cardIndex = ((InteractionAnswer.CardIndexChosen) answer).cardIndex();
        if (!interaction.validIndices().contains(cardIndex)) {
            throw new IllegalStateException("Invalid card index: " + cardIndex);
        }

        List<Card> hand = gameData.playerHands.get(interaction.playerId());
        if (hand == null || cardIndex >= hand.size()) {
            throw new IllegalStateException("Invalid card index: " + cardIndex);
        }

        Card selectedCard = hand.get(cardIndex);
        gameData.interaction.clearAwaitingInput();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                interaction.guessingPlayerId(), null, null,
                new ChoiceContext.MasterOfPredicamentsGuessChoice(
                        interaction.playerId(), interaction.sourceCard(), selectedCard),
                GUESS_OPTIONS,
                interaction.sourceCard().getName()
                        + " — Guess whether the chosen card's mana value is greater than 4."));
    }
}
