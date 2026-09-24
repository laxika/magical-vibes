package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.input.CardChoiceHandlerService;
import org.springframework.stereotype.Component;

/** Handles choosing a card in a revealed target hand for a perpetual ability. */
@Component
public class PerpetualTargetCardChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.PerpetualTargetCardChoice> {

    private final CardChoiceHandlerService cardChoiceHandlerService;

    public PerpetualTargetCardChoiceInteractionHandler(CardChoiceHandlerService cardChoiceHandlerService) {
        this.cardChoiceHandlerService = cardChoiceHandlerService;
    }

    @Override
    public Class<PendingInteraction.PerpetualTargetCardChoice> handledType() {
        return PendingInteraction.PerpetualTargetCardChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardIndexChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.PerpetualTargetCardChoice interaction,
                             InteractionAnswer answer) {
        cardChoiceHandlerService.handlePerpetualTargetCardChosen(
                gameData, player, ((InteractionAnswer.CardIndexChosen) answer).cardIndex());
    }
}
