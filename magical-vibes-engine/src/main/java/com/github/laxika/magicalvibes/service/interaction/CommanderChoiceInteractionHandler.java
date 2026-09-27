package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CommanderChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.CommanderChoice> {

    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<PendingInteraction.CommanderChoice> handledType() {
        return PendingInteraction.CommanderChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.CommanderChoice interaction, InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        List<UUID> cardIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (cardIds == null || cardIds.size() != 1) {
            throw new IllegalStateException("Choose exactly one commander");
        }
        UUID selectedId = cardIds.getFirst();
        if (!interaction.validCardIds().contains(selectedId)) {
            throw new IllegalStateException("Invalid commander: " + selectedId);
        }

        List<Card> commandZone = gameData.playerCommandZones.getOrDefault(player.getId(), List.of());
        Card selected = commandZone.stream()
                .filter(card -> card.getId().equals(selectedId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Selected commander is no longer in the command zone"));

        commandZone.remove(selected);
        gameData.addCardToHand(player.getId(), selected);
        gameData.interaction.clearAwaitingInput();
        gameLogService.append(gameData, GameLog.textCardText(
                player.getUsername() + " puts ", selected, " into their hand from the command zone."));
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
