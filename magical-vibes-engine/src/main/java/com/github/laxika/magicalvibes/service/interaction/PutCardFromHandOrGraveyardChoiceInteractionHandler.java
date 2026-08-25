package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.effect.normalfx.PutCardFromHandOrGraveyardOntoBattlefieldSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PutCardFromHandOrGraveyardChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.PutCardFromHandOrGraveyardChoice> {

    private final PutCardFromHandOrGraveyardOntoBattlefieldSupport support;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<PendingInteraction.PutCardFromHandOrGraveyardChoice> handledType() {
        return PendingInteraction.PutCardFromHandOrGraveyardChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.PutCardFromHandOrGraveyardChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        List<UUID> chosenIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (chosenIds == null) {
            chosenIds = List.of();
        }
        if (chosenIds.size() > 1
                || (chosenIds.size() == 1 && !interaction.validCardIds().contains(chosenIds.getFirst()))) {
            throw new IllegalStateException("Choose at most one valid card");
        }

        gameData.interaction.clearAwaitingInput();
        if (!chosenIds.isEmpty()) {
            support.applyChoice(gameData, player.getId(), chosenIds.getFirst(), interaction.cardName());
        }
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
