package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealPlanarCardsUntilTwoPlanesAndPlaneswalkEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RevealPlanarCardsUntilTwoPlanesAndPlaneswalkEffectHandler implements NormalEffectHandlerBean {

    private final PlanechaseService planechaseService;
    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealPlanarCardsUntilTwoPlanesAndPlaneswalkEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (gameData.planechase == null) {
            return;
        }

        List<Card> revealed = new ArrayList<>();
        int planeCount = 0;
        while (!gameData.planechase.deck.isEmpty() && planeCount < 2) {
            Card card = gameData.planechase.deck.removeFirst();
            revealed.add(card);
            if (card.hasType(CardType.PLANE)) {
                planeCount++;
            }
        }
        if (planeCount < 2) {
            gameData.planechase.deck.addAll(0, revealed);
            return;
        }

        List<Card> planes = revealed.stream().filter(card -> card.hasType(CardType.PLANE)).toList();
        List<Card> cardsToBottom = revealed.stream().filter(card -> !card.hasType(CardType.PLANE)).toList();
        gameLogService.append(gameData, GameLog.text("Planar cards are revealed until two planes are found: "
                + revealed.stream().map(Card::getName).collect(Collectors.joining(", ")) + "."));

        UUID controllerId = entry.getControllerId();
        if (cardsToBottom.size() <= 1) {
            planechaseService.completeSpatialMerging(gameData, planes, cardsToBottom, controllerId);
            return;
        }

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.SpatialMergingCardOrder(
                controllerId, planes, cardsToBottom,
                "Put the other revealed cards on the bottom of the planar deck in any order."));
    }
}
