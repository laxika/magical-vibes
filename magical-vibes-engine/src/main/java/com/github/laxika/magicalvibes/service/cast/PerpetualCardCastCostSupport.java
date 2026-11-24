package com.github.laxika.magicalvibes.service.cast;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;

/**
 * The one store of perpetual generic cast-cost changes, {@code GameData.perpetualGenericCastCostIncreases}, kept by
 * card identity without mutating frozen card objects: one signed amount per card, more generic mana to cast it when
 * positive, less when negative (digital only). A card whose changes cancel out has no entry.
 */
public final class PerpetualCardCastCostSupport {

    private PerpetualCardCastCostSupport() {
    }

    /** "It perpetually gains 'This spell costs {N} less to cast'". */
    public static void remember(GameData gameData, Card card, int amount) {
        if (amount > 0) {
            change(gameData, card, -amount);
        }
    }

    /** "It perpetually costs {N} more to cast". */
    public static void rememberIncrease(GameData gameData, Card card, int amount) {
        if (amount > 0) {
            change(gameData, card, amount);
        }
    }

    /** Adds {@code change} generic mana to what {@code card} perpetually costs more to cast; negative for less. */
    public static void change(GameData gameData, Card card, int change) {
        if (card == null || change == 0) {
            return;
        }
        gameData.perpetualGenericCastCostIncreases.merge(card.getId(), change,
                (current, added) -> current + added == 0 ? null : current + added);
    }

    /** How much more generic mana {@code card} perpetually costs to cast; negative for less. */
    public static int changeFor(GameData gameData, Card card) {
        return card == null ? 0 : gameData.perpetualGenericCastCostIncreases.getOrDefault(card.getId(), 0);
    }
}
