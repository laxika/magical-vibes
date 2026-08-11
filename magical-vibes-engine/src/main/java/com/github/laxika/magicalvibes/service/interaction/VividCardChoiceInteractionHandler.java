package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.effect.normalfx.VividRevealSupport;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Handles one optional per-color card selection during Vivid. */
@Component
@RequiredArgsConstructor
public class VividCardChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.VividCardChoice> {

    private final VividRevealSupport vividRevealSupport;

    @Override
    public Class<PendingInteraction.VividCardChoice> handledType() {
        return PendingInteraction.VividCardChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.VividCardChoice interaction,
                             InteractionAnswer answer) {
        List<UUID> cardIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        vividRevealSupport.handleChoice(gameData, interaction, cardIds);
    }
}
