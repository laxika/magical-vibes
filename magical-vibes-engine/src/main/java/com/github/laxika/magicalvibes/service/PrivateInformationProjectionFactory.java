package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.networking.message.RevealHandMessage;
import com.github.laxika.magicalvibes.networking.message.RevealLibraryTopMessage;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Builds restricted-audience reveal messages from authoritative post-mutation game state.
 */
@Component
@RequiredArgsConstructor
public class PrivateInformationProjectionFactory {

    private final CardViewFactory cardViewFactory;

    public RevealHandMessage createHandReveal(GameData gameData, UUID subjectPlayerId, List<Card> cards) {
        return new RevealHandMessage(createCardViews(cards), gameData.playerIdToName.get(subjectPlayerId));
    }

    public Object createReveal(GameData gameData, GameEventFact.PrivateReveal reveal) {
        List<CardView> cards = reveal.cards().stream()
                .map(snapshot -> findCard(gameData, snapshot.cardId()))
                .map(cardViewFactory::create)
                .toList();
        String playerName = gameData.playerIdToName.get(reveal.subjectPlayerId());
        return switch (reveal.zone()) {
            case HAND -> new RevealHandMessage(cards, playerName);
            case LIBRARY -> new RevealLibraryTopMessage(cards, playerName);
        };
    }

    private List<CardView> createCardViews(List<Card> cards) {
        return cards.stream().map(cardViewFactory::create).toList();
    }

    private Card findCard(GameData gameData, UUID cardId) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            Card card = findIn(gameData.playerHands.get(playerId), cardId);
            if (card != null) return card;
            card = findIn(gameData.playerDecks.get(playerId), cardId);
            if (card != null) return card;
            card = findIn(gameData.playerGraveyards.get(playerId), cardId);
            if (card != null) return card;
            for (Permanent permanent : gameData.playerBattlefields.getOrDefault(playerId, List.of())) {
                if (permanent.getCard().getId().equals(cardId)) {
                    return permanent.getCard();
                }
            }
        }
        for (ExiledCardEntry entry : gameData.exiledCards) {
            if (entry.card().getId().equals(cardId)) {
                return entry.card();
            }
        }
        for (StackEntry entry : gameData.stack) {
            if (entry.getCard().getId().equals(cardId)) {
                return entry.getCard();
            }
        }
        throw new IllegalStateException("Revealed card " + cardId + " is absent from authoritative game state");
    }

    private static Card findIn(List<Card> cards, UUID cardId) {
        if (cards == null) {
            return null;
        }
        for (Card card : new ArrayList<>(cards)) {
            if (card.getId().equals(cardId)) {
                return card;
            }
        }
        return null;
    }
}
