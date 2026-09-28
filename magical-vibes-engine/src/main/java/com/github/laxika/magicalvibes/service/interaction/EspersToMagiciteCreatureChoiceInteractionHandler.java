package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.CardType;
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

@Component
@RequiredArgsConstructor
public class EspersToMagiciteCreatureChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.EspersToMagiciteCreatureChoice> {

    private final EffectResolutionService effectResolutionService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<PendingInteraction.EspersToMagiciteCreatureChoice> handledType() {
        return PendingInteraction.EspersToMagiciteCreatureChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.EspersToMagiciteCreatureChoice interaction,
                             InteractionAnswer answer) {
        List<UUID> chosenIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (chosenIds == null || chosenIds.size() > 1
                || chosenIds.stream().anyMatch(cardId -> !interaction.validCardIds().contains(cardId))) {
            throw new IllegalStateException("Choose up to one creature card exiled this way");
        }

        StackEntry pendingEntry = gameData.pendingEffectResolutionEntry;
        if (pendingEntry == null) {
            throw new IllegalStateException("No pending effect resolution for Espers to Magicite's copy choice");
        }

        gameData.interaction.clearAwaitingInput();
        if (chosenIds.isEmpty()) {
            pendingEntry.setEventValue(-1);
        } else {
            UUID chosenId = chosenIds.getFirst();
            var exiled = gameData.findExiledCard(chosenId);
            if (exiled == null || !pendingEntry.getTargetCardIds().contains(chosenId)
                    || exiled.faceDown() || !exiled.card().hasType(CardType.CREATURE)) {
                throw new IllegalStateException("Chosen card is no longer a creature card exiled this way");
            }
            pendingEntry.setTargetId(chosenId);
        }

        gameData.rerunCurrentEffectAfterInteraction = false;
        effectResolutionService.resolveEffectsFrom(gameData, pendingEntry,
                gameData.pendingEffectResolutionIndex);
        if (!gameData.interaction.isAwaitingInput()) {
            inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
        }
    }
}
