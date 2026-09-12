package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealPlanarCardsUntilFivePlanesEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RevealPlanarCardsUntilFivePlanesEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealPlanarCardsUntilFivePlanesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (gameData.planechase == null) {
            return;
        }

        List<Card> revealed = new ArrayList<>();
        int planeCount = 0;
        while (!gameData.planechase.deck.isEmpty() && planeCount < 5) {
            Card card = gameData.planechase.deck.removeFirst();
            revealed.add(card);
            if (card.hasType(CardType.PLANE)) {
                planeCount++;
            }
        }
        if (revealed.isEmpty()) {
            return;
        }

        gameLogService.append(gameData, GameLog.text("Planar cards are revealed until five planes are found: "
                + revealed.stream().map(Card::getName).collect(Collectors.joining(", ")) + "."));

        UUID controllerId = entry.getControllerId();
        List<UUID> planeIds = revealed.stream()
                .filter(card -> card.hasType(CardType.PLANE))
                .map(Card::getId)
                .toList();
        if (planeIds.size() > 1) {
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.PlanarCardChoice(
                    controllerId, revealed, planeIds,
                    "Choose a plane to put on top of the planar deck."));
            return;
        }

        Card selected = planeIds.isEmpty()
                ? null
                : revealed.stream().filter(card -> card.getId().equals(planeIds.getFirst())).findFirst().orElseThrow();
        List<Card> remaining = new ArrayList<>(revealed);
        if (selected != null) {
            remaining.remove(selected);
            gameData.planechase.deck.add(selected);
        }
        Collections.shuffle(remaining);
        gameData.planechase.deck.addAll(remaining);
    }
}
