package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ExiledCardMayPlayChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.ExiledCardMayPlayChoice> {

    private final ExileSupport exileSupport;
    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<PendingInteraction.ExiledCardMayPlayChoice> handledType() {
        return PendingInteraction.ExiledCardMayPlayChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.ExiledCardMayPlayChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your choice to make");
        }

        List<UUID> chosenIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (chosenIds.size() != 1 || !interaction.validCardIds().contains(chosenIds.getFirst())) {
            throw new IllegalStateException("Choose exactly one card exiled this way");
        }

        UUID chosenId = chosenIds.getFirst();
        ExiledCardEntry chosen = gameData.findExiledCard(chosenId);
        if (chosen == null) {
            throw new IllegalStateException("Chosen card is no longer exiled");
        }

        gameData.interaction.clearAwaitingInput();
        exileSupport.grantPlayUntilOwnersNextTurn(gameData, chosenId, interaction.playerId());
        gameLogService.append(gameData, GameLog.cardThen(chosen.card(),
                " may be played until the end of its controller's next turn."));
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
