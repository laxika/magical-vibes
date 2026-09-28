package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealPlanarCardsUntilPlaneAndPlaneswalkEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RevealPlanarCardsUntilPlaneAndPlaneswalkEffectHandler implements NormalEffectHandlerBean {

    private final PlanechaseService planechaseService;
    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealPlanarCardsUntilPlaneAndPlaneswalkEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (gameData.planechase == null) {
            return;
        }

        List<Card> revealed = new ArrayList<>();
        Card plane = null;
        while (!gameData.planechase.deck.isEmpty()) {
            Card card = gameData.planechase.deck.removeFirst();
            revealed.add(card);
            if (card.hasType(CardType.PLANE)) {
                plane = card;
                break;
            }
        }
        if (revealed.isEmpty()) {
            return;
        }

        gameLogService.append(gameData, GameLog.text("Planar cards are revealed until a plane is found: "
                + revealed.stream().map(Card::getName).collect(Collectors.joining(", ")) + "."));

        Card foundPlane = plane;
        List<Card> cardsToBottom = foundPlane == null
                ? revealed
                : revealed.stream().filter(card -> card != foundPlane).toList();
        if (foundPlane == null) {
            putOnBottomOrPrompt(gameData, entry, cardsToBottom, null);
            return;
        }

        putOnBottomOrPrompt(gameData, entry, cardsToBottom, foundPlane);
    }

    private void putOnBottomOrPrompt(GameData gameData, StackEntry entry, List<Card> cardsToBottom,
                                     Card arrivingPlane) {
        if (cardsToBottom.size() <= 1) {
            if (arrivingPlane == null) {
                gameData.planechase.deck.addAll(cardsToBottom);
            } else {
                planechaseService.completePlaneswalkToPlaneWithoutDeparting(
                        gameData, arrivingPlane, cardsToBottom, entry.getControllerId());
            }
            return;
        }

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.PlanarDeckPlaneswalkCardOrder(
                entry.getControllerId(), arrivingPlane, cardsToBottom,
                arrivingPlane == null
                        ? "Put the revealed cards on the bottom of the planar deck in any order."
                        : "Put the other revealed cards on the bottom of the planar deck in any order."));
    }
}
