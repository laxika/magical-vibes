package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileSupport;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class HeistCardChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.HeistCardChoice> {

    private final ExileService exileService;
    private final ExileSupport exileSupport;
    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<PendingInteraction.HeistCardChoice> handledType() {
        return PendingInteraction.HeistCardChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.HeistCardChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your choice to make");
        }

        List<UUID> chosenIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (chosenIds.size() != 1 || !interaction.validCardIds().contains(chosenIds.getFirst())) {
            throw new IllegalStateException("Choose exactly one heisted card");
        }

        UUID chosenId = chosenIds.getFirst();
        Card chosen = interaction.cards().stream()
                .filter(card -> card.getId().equals(chosenId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Chosen card is not available"));
        List<Card> library = gameData.playerDecks.get(interaction.libraryOwnerId());
        if (library == null || !library.removeIf(card -> card.getId().equals(chosenId))) {
            throw new IllegalStateException("Chosen card is no longer in the library");
        }

        gameData.interaction.clearAwaitingInput();
        exileService.exileCardFaceDown(gameData, interaction.libraryOwnerId(), chosen, null,
                interaction.playerId());
        exileSupport.grantPlayWhileExiled(gameData, chosenId, interaction.playerId());
        gameData.exilePlayAnyManaTypeWhileExiled.add(chosenId);
        gameLogService.append(gameData, GameLog.text(
                gameData.playerIdToName.get(interaction.playerId()) + " heists a card from "
                        + gameData.playerIdToName.get(interaction.libraryOwnerId()) + "'s library."));
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
