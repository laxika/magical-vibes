package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.effect.normalfx.RemoveTimeCounterFromExiledCardEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.RemoveSuspendCounterFromExiledSpellEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SuspendedCardTimeCounterChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.SuspendedCardTimeCounterChoice> {

    private final RemoveTimeCounterFromExiledCardEffectHandler removeTimeCounterHandler;
    private final RemoveSuspendCounterFromExiledSpellEffectHandler removeSuspendedSpellCounterHandler;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<PendingInteraction.SuspendedCardTimeCounterChoice> handledType() {
        return PendingInteraction.SuspendedCardTimeCounterChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.SuspendedCardTimeCounterChoice interaction,
                             InteractionAnswer answer) {
        List<UUID> chosenIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (chosenIds == null || chosenIds.size() != 1 || !interaction.validCardIds().contains(chosenIds.getFirst())) {
            throw new IllegalStateException("Choose one suspended card you own");
        }

        UUID cardId = chosenIds.getFirst();
        ExiledCardEntry chosen = findEligibleCard(gameData, interaction.playerId(), cardId);
        if (chosen == null) {
            throw new IllegalStateException("Chosen suspended card is no longer available");
        }

        gameData.interaction.clearAwaitingInput();
        for (int i = 0; i < interaction.amount(); i++) {
            if (gameData.suspendedSpellExiles.stream().anyMatch(suspended -> suspended.cardId().equals(cardId))) {
                removeSuspendedSpellCounterHandler.removeTimeCounter(gameData, cardId);
            } else {
                removeTimeCounterHandler.removeTimeCounter(gameData, cardId);
            }
        }
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private ExiledCardEntry findEligibleCard(GameData gameData, UUID ownerId, UUID cardId) {
        synchronized (gameData.exiledCards) {
            for (ExiledCardEntry exiled : gameData.exiledCards) {
                if (ownerId.equals(exiled.ownerId()) && !exiled.faceDown()
                        && cardId.equals(exiled.card().getId())
                        && RemoveTimeCounterFromExiledCardEffectHandler.isSuspended(gameData, exiled)) {
                    return exiled;
                }
            }
        }
        return null;
    }
}
