package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Component
public class DrawFromLibraryPositionChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.DrawFromLibraryPositionChoice> {

    private final ObjectProvider<DrawService> drawServiceProvider;
    private final InputCompletionService inputCompletionService;

    public DrawFromLibraryPositionChoiceInteractionHandler(
            ObjectProvider<DrawService> drawServiceProvider,
            InputCompletionService inputCompletionService) {
        this.drawServiceProvider = drawServiceProvider;
        this.inputCompletionService = inputCompletionService;
    }

    @Override
    public Class<PendingInteraction.DrawFromLibraryPositionChoice> handledType() {
        return PendingInteraction.DrawFromLibraryPositionChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.NumberChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.DrawFromLibraryPositionChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        int position = ((InteractionAnswer.NumberChosen) answer).value();
        List<?> library = gameData.playerDecks.get(player.getId());
        int currentLibrarySize = library == null ? 0 : library.size();
        if (position < 1 || position > interaction.librarySize()
                || position > currentLibrarySize) {
            throw new IllegalArgumentException("Library position must be between 1 and "
                    + interaction.librarySize());
        }

        gameData.interaction.clearAwaitingInput();
        drawServiceProvider.getObject().resolveDrawCardFromLibraryPosition(
                gameData, player.getId(), position);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
