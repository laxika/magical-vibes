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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PlanarCardChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.PlanarCardChoice> {

    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<PendingInteraction.PlanarCardChoice> handledType() {
        return PendingInteraction.PlanarCardChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player, PendingInteraction.PlanarCardChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        List<UUID> cardIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (cardIds == null || cardIds.size() != 1) {
            throw new IllegalStateException("Choose exactly one plane");
        }
        UUID selectedId = cardIds.getFirst();
        if (!interaction.validPlaneCardIds().contains(selectedId)) {
            throw new IllegalStateException("Invalid plane card: " + selectedId);
        }

        Card selected = interaction.revealedCards().stream()
                .filter(card -> card.getId().equals(selectedId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Selected plane is no longer available"));
        List<Card> remaining = new ArrayList<>(interaction.revealedCards());
        remaining.remove(selected);

        if (gameData.planechase == null) {
            throw new IllegalStateException("No planar deck is active");
        }
        gameData.planechase.deck.add(selected);
        Collections.shuffle(remaining);
        gameData.planechase.deck.addAll(remaining);

        gameData.interaction.clearAwaitingInput();
        gameLogService.append(gameData, GameLog.text(player.getUsername() + " puts " + selected.getName()
                + " on top of the planar deck and the rest on the bottom in a random order."));
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
