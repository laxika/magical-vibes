package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.service.effect.EffectResolutionService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.MayCopyHandlerService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExiledCreatureCopyChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.ExiledCreatureCopyChoice> {

    private final EffectResolutionService effectResolutionService;
    private final InputCompletionService inputCompletionService;
    private final MayCopyHandlerService mayCopyHandlerService;

    @Override
    public Class<PendingInteraction.ExiledCreatureCopyChoice> handledType() {
        return PendingInteraction.ExiledCreatureCopyChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.ExiledCreatureCopyChoice interaction,
                             InteractionAnswer answer) {
        List<UUID> chosenIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (chosenIds == null || chosenIds.size() != 1
                || !interaction.validCardIds().contains(chosenIds.getFirst())) {
            throw new IllegalStateException("Choose one creature card exiled with " + interaction.sourceName());
        }

        ExiledCardEntry chosen = gameData.findExiledCard(chosenIds.getFirst());
        boolean sourceMatches = interaction.sourcePermanentId() == null
                ? chosen != null && gameData.exiledCardsWithTakeoverCounters.contains(chosenIds.getFirst())
                : chosen != null && interaction.sourcePermanentId().equals(chosen.sourcePermanentId());
        if (chosen == null || !sourceMatches
                || chosen.faceDown() || !chosen.card().hasType(CardType.CREATURE)) {
            throw new IllegalStateException("Chosen card is no longer a creature card exiled with "
                    + interaction.sourceName());
        }

        gameData.interaction.clearAwaitingInput();
        if (interaction.sourcePermanentId() == null) {
            mayCopyHandlerService.finishCloneEntryFromCard(gameData, chosen.card());
        } else {
            StackEntry pendingEntry = gameData.pendingEffectResolutionEntry;
            if (pendingEntry == null) {
                throw new IllegalStateException("No pending effect resolution for "
                        + interaction.sourceName() + "'s copy choice");
            }
            pendingEntry.setTargetId(chosenIds.getFirst());
            gameData.rerunCurrentEffectAfterInteraction = false;
            effectResolutionService.resolveEffectsFrom(gameData, pendingEntry,
                    gameData.pendingEffectResolutionIndex);
        }
        if (!gameData.interaction.isAwaitingInput()) {
            inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
        }
    }
}
