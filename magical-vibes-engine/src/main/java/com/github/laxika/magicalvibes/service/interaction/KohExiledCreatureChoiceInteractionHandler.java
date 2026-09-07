package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class KohExiledCreatureChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.KohExiledCreatureChoice> {

    private final GameQueryService gameQueryService;
    private final InputCompletionService inputCompletionService;

    public KohExiledCreatureChoiceInteractionHandler(
            GameQueryService gameQueryService,
            InputCompletionService inputCompletionService) {
        this.gameQueryService = gameQueryService;
        this.inputCompletionService = inputCompletionService;
    }

    @Override
    public Class<PendingInteraction.KohExiledCreatureChoice> handledType() {
        return PendingInteraction.KohExiledCreatureChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.KohExiledCreatureChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your choice to make");
        }

        List<UUID> chosenIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (chosenIds == null || chosenIds.size() != 1
                || !interaction.validCardIds().contains(chosenIds.getFirst())) {
            throw new IllegalStateException("Choose exactly one creature card exiled with Koh");
        }

        Permanent source = gameQueryService.findPermanentById(gameData, interaction.sourcePermanentId());
        ExiledCardEntry chosen = gameData.findExiledCard(chosenIds.getFirst());
        if (source == null || chosen == null
                || !interaction.sourcePermanentId().equals(chosen.sourcePermanentId())
                || chosen.faceDown() || !chosen.card().hasType(CardType.CREATURE)) {
            throw new IllegalStateException("Chosen card is not available with Koh");
        }

        gameData.interaction.clearAwaitingInput();
        source.setLastChosenExiledCard(chosen.card());
        inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }
}
