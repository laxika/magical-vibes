package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.effect.normalfx.BeholdEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BeholdChoiceInteractionHandler implements InteractionHandler<PendingInteraction.BeholdChoice> {

    private final BeholdEffectHandler effectHandler;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<PendingInteraction.BeholdChoice> handledType() {
        return PendingInteraction.BeholdChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.BeholdChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your choice to make");
        }
        List<UUID> chosen = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (chosen == null || chosen.size() != 1 || new HashSet<>(chosen).size() != 1
                || !interaction.validCardIds().containsAll(chosen)) {
            throw new IllegalArgumentException("Choose exactly one valid object to behold");
        }

        gameData.interaction.clearAwaitingInput();
        effectHandler.completeChoice(gameData, chosen.getFirst(), interaction);
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
