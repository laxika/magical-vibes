package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.input.CardChoiceHandlerService;
import org.springframework.stereotype.Component;

/** Applies a multi-card perpetual triggered-ability hand choice. */
@Component
public class PerpetualTriggeredAbilityCardsChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.PerpetualTriggeredAbilityCardsChoice> {

    private final CardChoiceHandlerService cardChoiceHandlerService;

    public PerpetualTriggeredAbilityCardsChoiceInteractionHandler(
            CardChoiceHandlerService cardChoiceHandlerService) {
        this.cardChoiceHandlerService = cardChoiceHandlerService;
    }

    @Override
    public Class<PendingInteraction.PerpetualTriggeredAbilityCardsChoice> handledType() {
        return PendingInteraction.PerpetualTriggeredAbilityCardsChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.PerpetualTriggeredAbilityCardsChoice interaction,
                             InteractionAnswer answer) {
        cardChoiceHandlerService.handlePerpetualTriggeredAbilityCardsChosen(
                gameData, player, ((InteractionAnswer.CardsChosen) answer).cardIds());
    }
}
