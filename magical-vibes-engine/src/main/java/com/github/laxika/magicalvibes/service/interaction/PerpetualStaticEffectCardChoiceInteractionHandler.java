package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.input.CardChoiceHandlerService;
import org.springframework.stereotype.Component;

/** Handles an optional hand-card choice for a perpetual static-effect grant. */
@Component
public class PerpetualStaticEffectCardChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.PerpetualStaticEffectCardChoice> {

    private final CardChoiceHandlerService cardChoiceHandlerService;

    public PerpetualStaticEffectCardChoiceInteractionHandler(CardChoiceHandlerService cardChoiceHandlerService) {
        this.cardChoiceHandlerService = cardChoiceHandlerService;
    }

    @Override
    public Class<PendingInteraction.PerpetualStaticEffectCardChoice> handledType() {
        return PendingInteraction.PerpetualStaticEffectCardChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardIndexChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.PerpetualStaticEffectCardChoice interaction,
                             InteractionAnswer answer) {
        cardChoiceHandlerService.handlePerpetualStaticEffectCardChosen(
                gameData, player, ((InteractionAnswer.CardIndexChosen) answer).cardIndex());
    }
}
