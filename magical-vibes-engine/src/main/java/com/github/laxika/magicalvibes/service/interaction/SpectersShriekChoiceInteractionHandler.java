package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.input.CardChoiceHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpectersShriekChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.SpectersShriekChoice> {

    private final CardChoiceHandlerService cardChoiceHandlerService;

    @Override
    public Class<PendingInteraction.SpectersShriekChoice> handledType() {
        return PendingInteraction.SpectersShriekChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardIndexChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.SpectersShriekChoice interaction,
                             InteractionAnswer answer) {
        cardChoiceHandlerService.handleSpectersShriekCardChosen(
                gameData, player, ((InteractionAnswer.CardIndexChosen) answer).cardIndex());
    }
}
