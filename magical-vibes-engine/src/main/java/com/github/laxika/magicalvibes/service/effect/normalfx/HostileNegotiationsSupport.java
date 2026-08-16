package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingHostileNegotiations;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Completes the two player choices that resolve Hostile Negotiations. */
@Component
@RequiredArgsConstructor
public class HostileNegotiationsSupport {

    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final InputCompletionService inputCompletionService;

    /** Prompts the opponent after the controller has selected the pile to turn face up. */
    public void revealPileAndPromptOpponent(
            GameData gameData, PendingInteraction.HostileNegotiationsFaceUpChoice choice,
            boolean pile1FaceUp) {
        PendingHostileNegotiations state = gameData.peekPendingInteraction(PendingHostileNegotiations.class);
        if (state == null) {
            throw new IllegalStateException("Missing Hostile Negotiations state");
        }

        String controllerName = gameData.playerIdToName.get(state.controllerId());
        gameLogService.append(gameData, GameLog.text(controllerName + " turns "
                + (pile1FaceUp ? "Pile 1" : "Pile 2") + " face up with Hostile Negotiations."));
        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.HostileNegotiationsOpponentPileChoice(
                        state.opponentId(), state.pile1Cards(), state.pile2Cards(), pile1FaceUp));
    }

    /** Moves the opponent's chosen pile to hand, the other pile to the graveyard, and resumes the spell. */
    public void completeOpponentChoice(
            GameData gameData, PendingInteraction.HostileNegotiationsOpponentPileChoice choice,
            boolean choosePile1) {
        PendingHostileNegotiations state = gameData.pollPendingInteraction(PendingHostileNegotiations.class);
        if (state == null) {
            throw new IllegalStateException("Missing Hostile Negotiations state");
        }

        List<Card> chosenPile = choosePile1 ? state.pile1Cards() : state.pile2Cards();
        List<Card> otherPile = choosePile1 ? state.pile2Cards() : state.pile1Cards();
        UUID controllerId = state.controllerId();
        for (Card card : chosenPile) {
            if (gameData.removeFromExile(card.getId())) {
                gameData.addCardToHand(controllerId, card);
            }
        }
        for (Card card : otherPile) {
            if (gameData.removeFromExile(card.getId())) {
                gameData.playerGraveyards.computeIfAbsent(controllerId, ignored -> new java.util.ArrayList<>())
                        .add(card);
            }
        }

        String controllerName = gameData.playerIdToName.get(controllerId);
        gameLogService.append(gameData, GameLog.text(controllerName + " takes "
                + (choosePile1 ? "Pile 1" : "Pile 2")
                + " into their hand and puts the other pile into their graveyard."));
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
