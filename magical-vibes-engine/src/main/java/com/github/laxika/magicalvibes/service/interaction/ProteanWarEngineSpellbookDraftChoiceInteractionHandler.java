package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProteanWarEngineSpellbookDraftChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.ProteanWarEngineSpellbookDraftChoice> {

    private final GameQueryService gameQueryService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<PendingInteraction.ProteanWarEngineSpellbookDraftChoice> handledType() {
        return PendingInteraction.ProteanWarEngineSpellbookDraftChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.ProteanWarEngineSpellbookDraftChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        List<UUID> chosenIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (chosenIds == null || chosenIds.size() != 1
                || !interaction.validCardIds().contains(chosenIds.getFirst())) {
            throw new IllegalStateException("Choose one card from Protean War Engine's spellbook");
        }

        UUID chosenId = chosenIds.getFirst();
        var chosen = interaction.cards().stream()
                .filter(card -> card.getId().equals(chosenId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Chosen spellbook card is no longer available"));

        gameData.addToExile(player.getId(), chosen, interaction.sourcePermanentId());
        gameData.interaction.clearAwaitingInput();
        Permanent source = gameQueryService.findPermanentById(gameData, interaction.sourcePermanentId());
        if (source != null) {
            battlefieldEntryService.processCreatureETBEffects(
                    gameData, player.getId(), source.getCard(), null, false);
        }
        if (!gameData.interaction.isAwaitingInput()) {
            inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
        }
    }
}
