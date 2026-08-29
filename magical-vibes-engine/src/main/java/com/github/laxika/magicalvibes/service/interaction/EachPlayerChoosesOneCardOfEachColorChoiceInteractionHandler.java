package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.effect.normalfx.EachPlayerChoosesOneCardOfEachColorThenDiscardsRestEffectHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EachPlayerChoosesOneCardOfEachColorChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.EachPlayerChoosesOneCardOfEachColorChoice> {

    private final EachPlayerChoosesOneCardOfEachColorThenDiscardsRestEffectHandler effectHandler;

    @Override
    public Class<PendingInteraction.EachPlayerChoosesOneCardOfEachColorChoice> handledType() {
        return PendingInteraction.EachPlayerChoosesOneCardOfEachColorChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardIndexChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.EachPlayerChoosesOneCardOfEachColorChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }
        effectHandler.completeChoice(gameData, interaction,
                ((InteractionAnswer.CardIndexChosen) answer).cardIndex());
    }
}
