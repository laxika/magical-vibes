package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.effect.normalfx.WorldsWithinWorldsSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WorldsWithinWorldsChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.WorldsWithinWorldsChoice> {

    private final WorldsWithinWorldsSupport support;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<PendingInteraction.WorldsWithinWorldsChoice> handledType() {
        return PendingInteraction.WorldsWithinWorldsChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.WorldsWithinWorldsChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        List<UUID> chosenCardIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (chosenCardIds == null) {
            chosenCardIds = List.of();
        }
        List<UUID> validated = new ArrayList<>();
        for (UUID cardId : chosenCardIds) {
            if (!interaction.validCardIds().contains(cardId)
                    || !new HashSet<>(validated).add(cardId)) {
                throw new IllegalStateException("Invalid or duplicate card ID: " + cardId);
            }
            validated.add(cardId);
        }

        Map<UUID, List<UUID>> chosenByPlayer = new LinkedHashMap<>(
                interaction.chosenCardIdsByPlayer());
        chosenByPlayer.put(player.getId(), List.copyOf(validated));

        gameData.interaction.clearAwaitingInput();
        boolean begunNext = support.beginNextChoice(gameData, interaction.remainingPlayerIds(),
                interaction.exiledCardIds(), chosenByPlayer, interaction.cardName());
        if (!begunNext) {
            support.finish(gameData, interaction.exiledCardIds(), chosenByPlayer, interaction.cardName());
        }
        inputCompletionService.publishStateAfterInput(gameData);
        if (!begunNext) {
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
        }
    }
}
