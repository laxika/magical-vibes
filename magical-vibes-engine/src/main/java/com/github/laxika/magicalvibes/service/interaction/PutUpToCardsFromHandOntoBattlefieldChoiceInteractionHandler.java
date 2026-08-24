package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.effect.normalfx.PutUpToCardsFromHandOntoBattlefieldSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PutUpToCardsFromHandOntoBattlefieldChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.PutUpToCardsFromHandOntoBattlefieldChoice> {

    private final PutUpToCardsFromHandOntoBattlefieldSupport support;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<PendingInteraction.PutUpToCardsFromHandOntoBattlefieldChoice> handledType() {
        return PendingInteraction.PutUpToCardsFromHandOntoBattlefieldChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.PutUpToCardsFromHandOntoBattlefieldChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        List<UUID> chosenIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (chosenIds == null) {
            chosenIds = List.of();
        }
        if (chosenIds.size() > interaction.maxCount()) {
            throw new IllegalStateException("Too many cards selected");
        }

        List<UUID> validated = new ArrayList<>();
        for (UUID id : chosenIds) {
            if (!interaction.validCardIds().contains(id) || !new HashSet<>(validated).add(id)) {
                throw new IllegalStateException("Invalid or duplicate card ID: " + id);
            }
            validated.add(id);
        }

        gameData.interaction.clearAwaitingInput();
        support.applyPutChoice(gameData, player.getId(), validated, interaction.cardName(), interaction.tapped());
        inputCompletionService.publishStateAfterInput(gameData);
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
