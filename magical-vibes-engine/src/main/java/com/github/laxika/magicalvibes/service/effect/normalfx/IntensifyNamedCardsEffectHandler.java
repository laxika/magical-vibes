package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.IntensifyNamedCardsEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Resolves the digital, zone-independent intensity mechanic. */
@Component
public class IntensifyNamedCardsEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    public IntensifyNamedCardsEffectHandler(GameQueryService gameQueryService) {
        this.gameQueryService = gameQueryService;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return IntensifyNamedCardsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        IntensifyNamedCardsEffect intensify = (IntensifyNamedCardsEffect) effect;
        UUID controllerId = entry.getControllerId();
        Set<UUID> visitedCardIds = new HashSet<>();

        gameData.playerBattlefields.forEach((controller, battlefield) -> {
            for (Permanent permanent : battlefield) {
                Card card = permanent.getCard();
                if (card == null || !visitedCardIds.add(card.getId())
                        || !isOwnedBy(card, controllerId, controller)
                        || !intensify.cardName().equals(gameQueryService.getEffectiveName(gameData, permanent))) {
                    continue;
                }
                gameData.intensifyCard(card, intensify.amount());
            }
        });

        visitZoneMap(gameData.playerDecks, controllerId, intensify, gameData, visitedCardIds);
        visitZoneMap(gameData.playerHands, controllerId, intensify, gameData, visitedCardIds);
        visitZoneMap(gameData.playerGraveyards, controllerId, intensify, gameData, visitedCardIds);
        visitZoneMap(gameData.playerSideboards, controllerId, intensify, gameData, visitedCardIds);
        visitZoneMap(gameData.playerCommandZones, controllerId, intensify, gameData, visitedCardIds);
        visitZoneMap(gameData.playerCommanders, controllerId, intensify, gameData, visitedCardIds);

        synchronized (gameData.exiledCards) {
            for (var exiled : gameData.exiledCards) {
                visitCard(exiled.card(), exiled.ownerId(), controllerId, intensify,
                        gameData, visitedCardIds);
            }
        }
        synchronized (gameData.stack) {
            for (StackEntry stackEntry : gameData.stack) {
                visitCard(stackEntry.getCard(), stackEntry.getControllerId(), controllerId, intensify,
                        gameData, visitedCardIds);
            }
        }
        for (Card card : gameData.subgameCards.values()) {
            visitCard(card, null, controllerId, intensify, gameData, visitedCardIds);
        }
    }

    private void visitZoneMap(java.util.Map<UUID, List<Card>> zones, UUID controllerId,
                              IntensifyNamedCardsEffect effect, GameData gameData,
                              Set<UUID> visitedCardIds) {
        zones.forEach((zoneOwner, cards) -> {
            for (Card card : cards) {
                visitCard(card, zoneOwner, controllerId, effect, gameData, visitedCardIds);
            }
        });
    }

    private void visitCard(Card card, UUID zoneOwner, UUID controllerId,
                           IntensifyNamedCardsEffect effect, GameData gameData,
                           Set<UUID> visitedCardIds) {
        if (card == null || !visitedCardIds.add(card.getId())
                || !isOwnedBy(card, controllerId, zoneOwner)
                || !effect.cardName().equals(card.getName())) {
            return;
        }
        gameData.intensifyCard(card, effect.amount());
    }

    private boolean isOwnedBy(Card card, UUID controllerId, UUID zoneOwner) {
        if (controllerId == null) return false;
        UUID ownerId = card.getOwnerId();
        return ownerId != null ? ownerId.equals(controllerId)
                : zoneOwner == null || zoneOwner.equals(controllerId);
    }
}
