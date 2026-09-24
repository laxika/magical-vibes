package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardPowerToughnessModifier;
import com.github.laxika.magicalvibes.model.GameData;

/** Stores perpetual P/T changes without mutating frozen card objects. */
public final class PerpetualCardPowerToughnessSupport {

    private PerpetualCardPowerToughnessSupport() {
    }

    public static void remember(GameData gameData, Card card, int powerBoost, int toughnessBoost) {
        if (card == null) {
            return;
        }
        gameData.perpetualCardPowerToughnessModifiers.merge(
                card.getId(),
                new CardPowerToughnessModifier(powerBoost, toughnessBoost),
                (current, added) -> current.add(added.power(), added.toughness()));
    }

}
