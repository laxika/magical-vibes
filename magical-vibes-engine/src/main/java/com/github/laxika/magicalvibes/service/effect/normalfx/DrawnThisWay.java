package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * A player's hand, library and draw history as a draw instruction begins, to tell afterwards which cards it drew: a
 * card a replacement returned (Words of Wind) or put into the hand from the library (Sages of the Anima) is a new
 * hand card but was not drawn (CR 614.6).
 */
record DrawnThisWay(UUID playerId, Set<UUID> hand, Set<UUID> library, int drawsBefore) {

    static DrawnThisWay before(GameData gameData, UUID playerId) {
        Set<UUID> hand = new HashSet<>();
        gameData.playerHands.getOrDefault(playerId, List.of()).forEach(card -> hand.add(card.getId()));
        Set<UUID> library = new HashSet<>();
        gameData.playerDecks.getOrDefault(playerId, List.of()).forEach(card -> library.add(card.getId()));
        return new DrawnThisWay(playerId, hand, library,
                gameData.cardsDrawnThisTurnIds.getOrDefault(playerId, List.of()).size());
    }

    /** The cards drawn since, as far as they are in the hand. */
    List<Card> cards(GameData gameData) {
        List<UUID> drawnNow = gameData.cardsDrawnThisTurnIds.getOrDefault(playerId, List.of()).stream()
                .skip(drawsBefore).toList();
        return gameData.playerHands.getOrDefault(playerId, List.of()).stream()
                .filter(card -> !hand.contains(card.getId()))
                .filter(card -> library.contains(card.getId()))
                .filter(card -> drawnNow.contains(card.getId()))
                .toList();
    }
}
