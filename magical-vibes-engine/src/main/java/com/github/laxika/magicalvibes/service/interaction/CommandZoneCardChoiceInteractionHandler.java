package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.effect.normalfx.PutCommanderIntoHandEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CommandZoneCardChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.CommandZoneCardChoice> {

    private final PutCommanderIntoHandEffectHandler effectHandler;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<PendingInteraction.CommandZoneCardChoice> handledType() {
        return PendingInteraction.CommandZoneCardChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.CommandZoneCardChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your choice to make");
        }

        List<UUID> chosen = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (chosen == null || chosen.size() != 1 || new HashSet<>(chosen).size() != 1
                || !interaction.validCardIds().contains(chosen.getFirst())) {
            throw new IllegalArgumentException("Choose exactly one commander");
        }

        gameData.interaction.clearAwaitingInput();
        effectHandler.completeChoice(gameData, interaction.playerId(), chosen.getFirst());
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
