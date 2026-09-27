package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutAllCommandersIntoHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PutAllCommandersIntoHandEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GraveyardService graveyardService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutAllCommandersIntoHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        Set<UUID> commanderIds = new HashSet<>();
        for (Card commander : gameData.playerCommanders.getOrDefault(controllerId, List.of())) {
            commanderIds.add(commander.getId());
        }

        List<Card> moved = new ArrayList<>();
        List<Card> commandZone = gameData.playerCommandZones.get(controllerId);
        if (commandZone != null) {
            for (Card card : new ArrayList<>(commandZone)) {
                if (commanderIds.contains(card.getId()) && commandZone.remove(card)) {
                    putIntoHand(gameData, controllerId, card);
                    moved.add(card);
                }
            }
        }

        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        if (graveyard != null) {
            List<Card> graveyardCommanders = graveyard.stream()
                    .filter(card -> commanderIds.contains(card.getId()))
                    .toList();
            if (!graveyardCommanders.isEmpty()) {
                graveyardService.beginGraveyardLeaveBatch(gameData);
                try {
                    for (Card card : graveyardCommanders) {
                        if (graveyard.remove(card)) {
                            graveyardService.notifyCardsLeftGraveyard(gameData, controllerId, card);
                            putIntoHand(gameData, controllerId, card);
                            moved.add(card);
                        }
                    }
                } finally {
                    graveyardService.endGraveyardLeaveBatch(gameData);
                }
            }
        }

        if (!moved.isEmpty()) {
            GameLog.Builder log = GameLog.builder().text("Puts ");
            for (int i = 0; i < moved.size(); i++) {
                if (i > 0) {
                    log.text(", ");
                }
                log.card(moved.get(i));
            }
            log.text(" into their hand.");
            gameLogService.append(gameData, log.build());
        }
    }

    private void putIntoHand(GameData gameData, UUID playerId, Card card) {
        UUID previousMove = gameData.completingCommanderZoneMove;
        gameData.completingCommanderZoneMove = card.getId();
        try {
            gameData.addCardToHand(playerId, card);
        } finally {
            gameData.completingCommanderZoneMove = previousMove;
        }
    }
}
