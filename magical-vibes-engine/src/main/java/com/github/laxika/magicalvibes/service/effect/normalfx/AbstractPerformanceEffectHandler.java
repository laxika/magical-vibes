package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardPileDisposition;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.PendingPileSeparation;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AbstractPerformanceEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AbstractPerformanceEffectHandler implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AbstractPerformanceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        String controllerName = gameData.playerIdToName.get(controllerId);
        List<Card> deck = gameData.playerDecks.get(controllerId);
        List<Card> faceDownPile = new ArrayList<>();
        List<Card> faceUpPile = new ArrayList<>();

        for (int i = 0; i < 4 && deck != null && !deck.isEmpty(); i++) {
            Card card = deck.removeFirst();
            faceDownPile.add(card);
            exileService.exileCardFaceDown(gameData, controllerId, card, null);
        }
        for (int i = 0; i < 4 && deck != null && !deck.isEmpty(); i++) {
            Card card = deck.removeFirst();
            faceUpPile.add(card);
            exileService.exileCard(gameData, controllerId, card);
        }

        List<Card> allCards = new ArrayList<>(faceDownPile);
        allCards.addAll(faceUpPile);
        if (allCards.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(controllerName + "'s library is empty."));
            return;
        }

        gameLogService.append(gameData, GameLog.text(controllerName + " exiles the top "
                + allCards.size() + " cards of their library in two piles."));

        UUID opponentId = gameData.orderedPlayerIds.stream()
                .filter(id -> !id.equals(controllerId))
                .findFirst()
                .orElse(null);
        if (opponentId == null) {
            return;
        }

        Map<UUID, UUID> cardOwners = allCards.stream()
                .collect(Collectors.toMap(Card::getId, card -> controllerId));
        gameData.queueInteraction(new PendingPileSeparation(controllerId, opponentId, List.of(), allCards,
                cardOwners, faceDownPile.stream().map(Card::getId).toList(),
                faceUpPile.stream().map(Card::getId).toList(),
                CardPileDisposition.GRAVEYARD_AND_FREE_CAST_ONE_REST_TO_HAND, false));

        String faceUpNames = faceUpPile.stream().map(Card::getName).collect(Collectors.joining(", "));
        String prompt = "Choose a pile to put into " + controllerName + "'s graveyard. Yes = face-down pile ("
                + faceDownPile.size() + " cards), No = face-up pile (" + faceUpNames + ").";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(entry.getCard(), opponentId, List.of(), prompt));
        playerInputService.processNextMayAbility(gameData);
    }
}
