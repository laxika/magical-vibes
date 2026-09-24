package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.effect.normalfx.ApplejackFamilyGatheringEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Handles Applejack's mandatory outside-the-game toy choice. */
@Component
public class ApplejackToyChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.ApplejackToyChoice> {

    private final ApplejackFamilyGatheringEffectHandler effectHandler;
    private final InputCompletionService inputCompletionService;

    public ApplejackToyChoiceInteractionHandler(
            ApplejackFamilyGatheringEffectHandler effectHandler,
            InputCompletionService inputCompletionService) {
        this.effectHandler = effectHandler;
        this.inputCompletionService = inputCompletionService;
    }

    @Override
    public Class<PendingInteraction.ApplejackToyChoice> handledType() {
        return PendingInteraction.ApplejackToyChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.ApplejackToyChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your choice");
        }

        List<UUID> cardIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (cardIds == null || cardIds.size() != 1
                || !interaction.validCardIds().contains(cardIds.getFirst())) {
            throw new IllegalStateException("Choose one toy you own");
        }

        Card chosenToy = interaction.toys().stream()
                .filter(card -> card.getId().equals(cardIds.getFirst()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Chosen toy is no longer available"));
        boolean stillOwned = gameData.playerSideboards
                .getOrDefault(interaction.playerId(), List.of()).stream()
                .anyMatch(card -> card.getId().equals(chosenToy.getId()));
        if (!stillOwned) {
            throw new IllegalStateException("Chosen toy is no longer available");
        }

        gameData.interaction.clearAwaitingInput();
        effectHandler.completeChoice(gameData, chosenToy);
        inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }
}
