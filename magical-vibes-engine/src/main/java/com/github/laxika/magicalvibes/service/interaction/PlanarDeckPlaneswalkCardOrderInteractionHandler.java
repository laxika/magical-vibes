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
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class PlanarDeckPlaneswalkCardOrderInteractionHandler
        implements InteractionHandler<PendingInteraction.PlanarDeckPlaneswalkCardOrder> {

    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;
    private final PlanechaseService planechaseService;

    @Override
    public Class<PendingInteraction.PlanarDeckPlaneswalkCardOrder> handledType() {
        return PendingInteraction.PlanarDeckPlaneswalkCardOrder.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardOrder.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.PlanarDeckPlaneswalkCardOrder interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your turn to reorder");
        }

        List<Integer> cardOrder = ((InteractionAnswer.CardOrder) answer).cardOrder();
        int count = interaction.cardsToBottom().size();
        if (cardOrder.size() != count) {
            throw new IllegalStateException("Must specify order for all " + count + " cards");
        }
        Set<Integer> seen = new HashSet<>();
        for (int index : cardOrder) {
            if (index < 0 || index >= count || !seen.add(index)) {
                throw new IllegalStateException("Invalid card order");
            }
        }

        if (gameData.planechase == null) {
            throw new IllegalStateException("No planar deck is active");
        }

        List<Card> orderedCards = cardOrder.stream().map(interaction.cardsToBottom()::get).toList();
        gameData.interaction.clearAwaitingInput();
        if (interaction.arrivingPlane() == null) {
            gameData.planechase.deck.addAll(orderedCards);
        } else {
            planechaseService.completePlaneswalkToPlaneWithoutDeparting(
                    gameData, interaction.arrivingPlane(), orderedCards, interaction.playerId());
        }
        gameLogService.append(gameData, GameLog.text(player.getUsername()
                + " orders the revealed cards on the bottom of the planar deck."));
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
