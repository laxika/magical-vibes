package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.input.CardChoiceHandlerService;
import org.springframework.stereotype.Component;

/** Handles the mandatory hand-card choice made by Unsavory Kitchen-style effects. */
@Component
public class PerpetualCreatureCardChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.PerpetualCreatureCardChoice> {

    private final CardChoiceHandlerService cardChoiceHandlerService;

    public PerpetualCreatureCardChoiceInteractionHandler(CardChoiceHandlerService cardChoiceHandlerService) {
        this.cardChoiceHandlerService = cardChoiceHandlerService;
    }

    @Override
    public Class<PendingInteraction.PerpetualCreatureCardChoice> handledType() {
        return PendingInteraction.PerpetualCreatureCardChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardIndexChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.PerpetualCreatureCardChoice interaction,
                             InteractionAnswer answer) {
        cardChoiceHandlerService.handlePerpetualCreatureCardChosen(
                gameData, player, ((InteractionAnswer.CardIndexChosen) answer).cardIndex());
    }
}
