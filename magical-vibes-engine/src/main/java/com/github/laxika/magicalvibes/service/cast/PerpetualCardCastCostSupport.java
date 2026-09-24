package com.github.laxika.magicalvibes.service.cast;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;

/** Stores perpetual generic cast-cost changes without mutating frozen card objects. */
public final class PerpetualCardCastCostSupport {

    private PerpetualCardCastCostSupport() {
    }

    public static void remember(GameData gameData, Card card, int amount) {
        if (card == null || amount <= 0) {
            return;
        }
        gameData.perpetualCardCastCostReductions.merge(card.getId(), amount, Integer::sum);
    }

    public static int reductionFor(GameData gameData, Card card) {
        return card == null ? 0 : gameData.perpetualCardCastCostReductions.getOrDefault(card.getId(), 0);
    }

    public static void rememberIncrease(GameData gameData, Card card, int amount) {
        if (card == null || amount <= 0) {
            return;
        }
        gameData.perpetualCardCastCostIncreases.merge(card.getId(), amount, Integer::sum);
    }

    public static int increaseFor(GameData gameData, Card card) {
        return card == null ? 0 : gameData.perpetualCardCastCostIncreases.getOrDefault(card.getId(), 0);
    }
}
