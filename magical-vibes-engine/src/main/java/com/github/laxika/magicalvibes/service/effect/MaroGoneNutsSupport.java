package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DoublingEffect;
import com.github.laxika.magicalvibes.model.effect.MaroGoneNutsEffect;

/** Shared rules support for the playtest card Maro's Gone Nuts. */
public final class MaroGoneNutsSupport {

    private MaroGoneNutsSupport() {
    }

    /** Returns the factor by which each doubling effect is upgraded on the current battlefield. */
    public static int doublingFactor(GameData gameData) {
        int[] count = {0};
        gameData.forEachPermanent((playerId, permanent) -> {
            for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof MaroGoneNutsEffect) {
                    count[0]++;
                }
            }
        });
        return powerOfTwo(count[0]);
    }

    /** Returns whether an effect is one of the effects Maro's Gone Nuts modifies. */
    public static boolean isDoublingEffect(CardEffect effect) {
        return effect instanceof DoublingEffect doubling && doubling.isDoublingEffect();
    }

    /** Applies Maro's multiplier to the result of one doubling effect. */
    public static int apply(GameData gameData, CardEffect effect, int value) {
        return isDoublingEffect(effect) ? value * doublingFactor(gameData) : value;
    }

    private static int powerOfTwo(int exponent) {
        int result = 1;
        for (int i = 0; i < exponent; i++) {
            result *= 2;
        }
        return result;
    }
}
