package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.effect.normalfx.NivMizzetRevealSupport;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Handles Niv-Mizzet's one-card-per-color-pair selection. */
@Component
@RequiredArgsConstructor
public class NivMizzetColorPairChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.NivMizzetColorPairChoice> {

    private final NivMizzetRevealSupport nivMizzetRevealSupport;

    @Override
    public Class<PendingInteraction.NivMizzetColorPairChoice> handledType() {
        return PendingInteraction.NivMizzetColorPairChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.NivMizzetColorPairChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your choice to make");
        }
        List<UUID> cardIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        nivMizzetRevealSupport.handleChoice(gameData, interaction, cardIds);
    }
}
