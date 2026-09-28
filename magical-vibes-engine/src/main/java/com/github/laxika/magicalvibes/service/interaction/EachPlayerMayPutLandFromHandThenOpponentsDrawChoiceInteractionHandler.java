package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.effect.normalfx.EachPlayerMayPutLandFromHandThenOpponentsDrawSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EachPlayerMayPutLandFromHandThenOpponentsDrawChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.EachPlayerMayPutLandFromHandThenOpponentsDrawChoice> {

    private final EachPlayerMayPutLandFromHandThenOpponentsDrawSupport support;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<PendingInteraction.EachPlayerMayPutLandFromHandThenOpponentsDrawChoice> handledType() {
        return PendingInteraction.EachPlayerMayPutLandFromHandThenOpponentsDrawChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.EachPlayerMayPutLandFromHandThenOpponentsDrawChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        List<UUID> chosen = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (chosen == null) {
            chosen = List.of();
        }
        if (chosen.size() > 1 || !interaction.validCardIds().containsAll(chosen)
                || chosen.stream().distinct().count() != chosen.size()) {
            throw new IllegalStateException("Choose zero or one valid land card");
        }

        var chosenCardIdsByPlayer = new java.util.LinkedHashMap<>(interaction.chosenCardIdsByPlayer());
        if (!chosen.isEmpty()) {
            chosenCardIdsByPlayer.put(player.getId(), chosen.getFirst());
        }
        gameData.interaction.clearAwaitingInput();

        boolean begunNext = support.beginNextChoice(gameData, interaction.remainingPlayerIds(),
                chosenCardIdsByPlayer, interaction.sourceControllerId(), interaction.cardName());
        inputCompletionService.publishStateAfterInput(gameData);
        if (!begunNext) {
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
        }
    }
}
