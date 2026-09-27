package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.service.effect.EffectResolutionService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Completes Mari's choice of an exiled card with a hit counter. */
@Component
@RequiredArgsConstructor
public class HitCounterExiledCardChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.HitCounterExiledCardChoice> {

    private final EffectResolutionService effectResolutionService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<PendingInteraction.HitCounterExiledCardChoice> handledType() {
        return PendingInteraction.HitCounterExiledCardChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.HitCounterExiledCardChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your choice");
        }

        List<UUID> chosenIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (chosenIds == null || chosenIds.size() != 1
                || !interaction.validCardIds().contains(chosenIds.getFirst())) {
            throw new IllegalStateException("Choose one exiled card with a hit counter");
        }

        UUID chosenId = chosenIds.getFirst();
        ExiledCardEntry chosen = gameData.findExiledCard(chosenId);
        int hitCounters = gameData.exiledCardHitCounters.getOrDefault(chosenId, 0);
        if (chosen == null || !interaction.ownerId().equals(chosen.ownerId())
                || chosen.faceDown() || hitCounters <= 0) {
            throw new IllegalStateException("Chosen card no longer has a hit counter");
        }

        gameData.exiledCardHitCounters.compute(chosenId,
                (id, count) -> count == null || count <= 1 ? null : count - 1);

        StackEntry pendingEntry = gameData.pendingEffectResolutionEntry;
        if (pendingEntry == null) {
            throw new IllegalStateException("No pending effect resolution for hit-counter choice");
        }
        if (interaction.followUpEffect() != null) {
            pendingEntry.insertEffectsToResolve(gameData.pendingEffectResolutionIndex,
                    List.of(interaction.followUpEffect()));
        }

        gameData.interaction.clearAwaitingInput();
        gameData.rerunCurrentEffectAfterInteraction = false;
        effectResolutionService.resolveEffectsFrom(gameData, pendingEntry,
                gameData.pendingEffectResolutionIndex);
        if (!gameData.interaction.isAwaitingInput()) {
            inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
        }
    }
}
