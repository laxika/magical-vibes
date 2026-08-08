package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Puts the cards a permanent exiled on the bottom of their owners' libraries in a random order.
 * Used by Possibility Storm's "then they put all cards exiled with this enchantment on the bottom
 * of their library in a random order" clause.
 */
@Component
@RequiredArgsConstructor
public class ExileBottomRandomSupport {

    private final GameLogService gameLogService;

    /**
     * Moves every card exiled with {@code sourcePermanentId} — except {@code exceptCardId}, which may
     * be {@code null} — to the bottom of its owner's library in a random order. Each owner's cards
     * are shuffled independently before being appended.
     */
    public void bottomCardsExiledWithSource(GameData gameData, UUID sourcePermanentId, UUID exceptCardId) {
        List<Card> exiled = new ArrayList<>(gameData.getCardsExiledByPermanent(sourcePermanentId));
        if (exiled.isEmpty()) {
            return;
        }

        Map<UUID, List<Card>> byOwner = new HashMap<>();
        for (Card card : exiled) {
            if (exceptCardId != null && card.getId().equals(exceptCardId)) {
                continue;
            }
            ExiledCardEntry entry = gameData.findExiledCard(card.getId());
            if (entry == null) {
                continue;
            }
            gameData.removeFromExile(card.getId());
            byOwner.computeIfAbsent(entry.ownerId(), id -> new ArrayList<>()).add(card);
        }

        byOwner.forEach((ownerId, cards) -> {
            List<Card> deck = gameData.playerDecks.get(ownerId);
            if (deck == null) {
                return;
            }
            Collections.shuffle(cards);
            deck.addAll(cards);
            gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(ownerId)
                    + " puts " + cards.size() + " exiled card" + (cards.size() == 1 ? "" : "s")
                    + " on the bottom of their library in a random order."));
        });
    }
}
