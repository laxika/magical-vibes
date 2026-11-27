package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ChosenOpponentFollowUp;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingHostileNegotiations;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Completes the two player choices that resolve Hostile Negotiations. */
@Component
@RequiredArgsConstructor
public class HostileNegotiationsSupport
        implements ChosenOpponentContinuation<ChosenOpponentFollowUp.HostileNegotiationsPiles> {

    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final InputCompletionService inputCompletionService;
    private final GraveyardService graveyardService;
    private final ChosenOpponentSupport chosenOpponentSupport;

    /** Turns the controller's pile face up, then the controller chooses the opponent who chooses a pile. */
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
        chosenOpponentSupport.chooseThen(gameData, state.controllerId(), "Hostile Negotiations",
                new ChosenOpponentFollowUp.HostileNegotiationsPiles(pile1FaceUp));
    }

    @Override
    public Class<ChosenOpponentFollowUp.HostileNegotiationsPiles> followUpType() {
        return ChosenOpponentFollowUp.HostileNegotiationsPiles.class;
    }

    /**
     * "An opponent chooses one of those piles": the chosen opponent is asked. With no opponent left, nobody chooses,
     * so neither pile is put into the hand or the graveyard and the cards stay exiled.
     */
    @Override
    public void continueWithOpponent(GameData gameData, UUID opponentId,
                                     ChosenOpponentFollowUp.HostileNegotiationsPiles piles) {
        if (opponentId == null) {
            gameData.pollPendingInteraction(PendingHostileNegotiations.class);
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }
        PendingHostileNegotiations state = gameData.peekPendingInteraction(PendingHostileNegotiations.class);
        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.HostileNegotiationsOpponentPileChoice(
                        opponentId, state.pile1Cards(), state.pile2Cards(), piles.pile1FaceUp()));
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
                graveyardService.addCardToGraveyard(gameData, controllerId, card, Zone.EXILE);
            }
        }

        String controllerName = gameData.playerIdToName.get(controllerId);
        gameLogService.append(gameData, GameLog.text(controllerName + " takes "
                + (choosePile1 ? "Pile 1" : "Pile 2")
                + " into their hand and puts the other pile into their graveyard."));
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
