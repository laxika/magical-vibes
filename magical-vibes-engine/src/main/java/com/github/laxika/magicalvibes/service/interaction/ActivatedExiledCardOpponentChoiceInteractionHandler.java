package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Handles the controller's choice of which opponent makes an activated exiled-card choice. */
@Component
@RequiredArgsConstructor
public class ActivatedExiledCardOpponentChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.ActivatedExiledCardOpponentChoice> {

    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<PendingInteraction.ActivatedExiledCardOpponentChoice> handledType() {
        return PendingInteraction.ActivatedExiledCardOpponentChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.PermanentsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.ActivatedExiledCardOpponentChoice interaction,
                             InteractionAnswer answer) {
        if (!interaction.controllerId().equals(player.getId())) {
            throw new IllegalStateException("Not the controller's choice");
        }
        List<UUID> selected = ((InteractionAnswer.PermanentsChosen) answer).permanentIds();
        if (selected == null || selected.size() != 1 || !interaction.opponentIds().contains(selected.getFirst())) {
            throw new IllegalStateException("Choose one opponent");
        }

        gameData.interaction.clearAwaitingInput();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ActivatedExiledCardChoice(
                selected.getFirst(), interaction.controllerId(), interaction.validCardIds(),
                interaction.sourceName()));
    }
}
