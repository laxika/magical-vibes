package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.service.effect.EffectResolutionService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SpellbookDraftToExileChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.SpellbookDraftToExileChoice> {

    private final EffectResolutionService effectResolutionService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<PendingInteraction.SpellbookDraftToExileChoice> handledType() {
        return PendingInteraction.SpellbookDraftToExileChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.SpellbookDraftToExileChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        List<UUID> chosenIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (chosenIds == null || chosenIds.size() != 1
                || !interaction.validCardIds().contains(chosenIds.getFirst())) {
            throw new IllegalStateException("Choose one card from the spellbook");
        }

        UUID chosenId = chosenIds.getFirst();
        Card chosen = interaction.cards().stream()
                .filter(card -> card.getId().equals(chosenId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Chosen spellbook card is no longer available"));
        StackEntry pendingEntry = gameData.pendingEffectResolutionEntry;
        if (pendingEntry == null) {
            throw new IllegalStateException("No pending spellbook effect");
        }

        gameData.addToExile(player.getId(), chosen, interaction.sourcePermanentId(), true);
        gameData.interaction.clearAwaitingInput();
        pendingEntry.setTargetId(chosenId);
        gameData.rerunCurrentEffectAfterInteraction = false;
        effectResolutionService.resolveEffectsFrom(gameData, pendingEntry,
                gameData.pendingEffectResolutionIndex);
        if (!gameData.interaction.isAwaitingInput()) {
            inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
        }
    }
}
