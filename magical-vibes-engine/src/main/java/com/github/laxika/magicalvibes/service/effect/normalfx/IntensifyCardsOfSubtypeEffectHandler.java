package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.IntensifyCardsOfSubtypeEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Resolves subtype-based persistent intensity increases across the controller's owned zones. */
@Component
public class IntensifyCardsOfSubtypeEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    public IntensifyCardsOfSubtypeEffectHandler(GameQueryService gameQueryService) {
        this.gameQueryService = gameQueryService;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return IntensifyCardsOfSubtypeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        IntensifyCardsOfSubtypeEffect intensify = (IntensifyCardsOfSubtypeEffect) effect;
        UUID controllerId = entry.getControllerId();
        Set<UUID> visitedCardIds = new HashSet<>();

        // The resolving spell has already left the stack, but has not yet been disposed into its
        // final zone. It is still an owned card and must receive the intensity increase.
        visitCard(entry.getCard(), controllerId, controllerId, intensify, gameData, visitedCardIds);

        gameData.playerBattlefields.forEach((controller, battlefield) -> {
            for (Permanent permanent : battlefield) {
                Card card = permanent.getCard();
                if (card != null && visitedCardIds.add(card.getId())
                        && isOwnedBy(card, controllerId, controller)
                        && GameQueryService.permanentHasSubtype(permanent, intensify.subtype())) {
                    gameData.intensifyCard(card, intensify.amount());
                }
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
                visitCard(stackEntry.getCard(), stackEntry.getControllerId(), controllerId,
                        intensify, gameData, visitedCardIds);
            }
        }
        for (Card card : gameData.subgameCards.values()) {
            visitCard(card, null, controllerId, intensify, gameData, visitedCardIds);
        }
    }

    private void visitZoneMap(java.util.Map<UUID, List<Card>> zones, UUID controllerId,
                              IntensifyCardsOfSubtypeEffect effect, GameData gameData,
                              Set<UUID> visitedCardIds) {
        zones.forEach((zoneOwner, cards) -> {
            for (Card card : cards) {
                visitCard(card, zoneOwner, controllerId, effect, gameData, visitedCardIds);
            }
        });
    }

    private void visitCard(Card card, UUID zoneOwner, UUID controllerId,
                           IntensifyCardsOfSubtypeEffect effect, GameData gameData,
                           Set<UUID> visitedCardIds) {
        if (card == null || !visitedCardIds.add(card.getId())) {
            return;
        }
        boolean owned = isOwnedBy(card, controllerId, zoneOwner);
        boolean subtype = gameQueryService.cardHasSubtype(card, effect.subtype(), gameData, zoneOwner);
        if (!owned || !subtype) {
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
