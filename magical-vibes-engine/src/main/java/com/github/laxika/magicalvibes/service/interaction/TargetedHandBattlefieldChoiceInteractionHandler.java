package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.input.CardChoiceHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TargetedHandBattlefieldChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.TargetedHandBattlefieldChoice> {

    private final CardChoiceHandlerService cardChoiceHandlerService;

    @Override
    public Class<PendingInteraction.TargetedHandBattlefieldChoice> handledType() {
        return PendingInteraction.TargetedHandBattlefieldChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardIndexChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.TargetedHandBattlefieldChoice interaction,
                             InteractionAnswer answer) {
        cardChoiceHandlerService.handleTargetedHandBattlefieldCardChosen(
                gameData, player, ((InteractionAnswer.CardIndexChosen) answer).cardIndex());
    }
}
