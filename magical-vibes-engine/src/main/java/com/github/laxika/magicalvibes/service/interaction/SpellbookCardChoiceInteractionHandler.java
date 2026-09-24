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
public class SpellbookCardChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.SpellbookCardChoice> {

    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<PendingInteraction.SpellbookCardChoice> handledType() {
        return PendingInteraction.SpellbookCardChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.SpellbookCardChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        List<UUID> cardIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (cardIds == null || cardIds.size() != 1) {
            throw new IllegalStateException("Choose exactly one spellbook card");
        }

        UUID selectedId = cardIds.getFirst();
        if (!interaction.validCardIds().contains(selectedId)) {
            throw new IllegalStateException("Invalid spellbook card: " + selectedId);
        }

        Card selected = interaction.spellbookCards().stream()
                .filter(card -> card.getId().equals(selectedId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Selected spellbook card is no longer available"));
        gameData.addCardToHand(player.getId(), selected);
        gameData.interaction.clearAwaitingInput();
        gameLogService.append(gameData, GameLog.text(player.getUsername() + " conjures "
                + selected.getName() + " into their hand."));
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
