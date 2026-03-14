package com.github.laxika.magicalvibes.service.exile;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

@Service
public class ExileService {

    /**
     * Exiles a card, adding it to the specified player's exile zone.
     */
    public void exileCard(GameData gameData, UUID ownerId, Card card) {
        gameData.playerExiledCards
                .computeIfAbsent(ownerId, k -> Collections.synchronizedList(new ArrayList<>()))
                .add(card);
    }
}
