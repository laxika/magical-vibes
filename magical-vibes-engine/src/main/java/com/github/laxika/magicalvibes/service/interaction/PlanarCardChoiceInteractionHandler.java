package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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

    @Autowired
    @Lazy
    private PlanechaseService planechaseService;

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
            throw new IllegalStateException("Choose exactly one card");
        }
        UUID selectedId = cardIds.getFirst();
        if (!interaction.validPlaneCardIds().contains(selectedId)) {
            throw new IllegalStateException("Invalid plane card: " + selectedId);
        }

        Card selected = interaction.revealedCards().stream()
                .filter(card -> card.getId().equals(selectedId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Selected plane is no longer available"));

        if (interaction.planeswalkAfterChoice()) {
            if (interaction.revealedCards().size() != 2 || gameData.planechase == null) {
                throw new IllegalStateException("Invalid planeswalk replacement choice");
            }
            Card remaining = interaction.revealedCards().stream()
                    .filter(card -> !card.getId().equals(selectedId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Missing second planar card"));
            gameData.planechase.deck.add(selected);
            gameData.planechase.deck.add(0, remaining);
            gameData.interaction.clearAwaitingInput();
            gameLogService.append(gameData, GameLog.text(player.getUsername()
                    + " puts one planar card on the bottom and one on top, then planeswalks."));
            planechaseService.continuePlaneswalk(gameData);
            if (!gameData.interaction.isAwaitingInput()) {
                inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            }
            return;
        }

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
