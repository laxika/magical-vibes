package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Each opponent of the player who caused the trigger draws {@code amount} cards.
 *
 * <p>The triggering player is read from the spell-cast event associated with the stack entry, so
 * this effect is useful for wording such as "each of that player's opponents".</p>
 */
public record DrawCardsForEachOpponentOfTriggeringPlayerEffect(DynamicAmount amount)
        implements CardDrawingEffect {

    public DrawCardsForEachOpponentOfTriggeringPlayerEffect(int amount) {
        this(new Fixed(amount));
    }

    @Override
    public DynamicAmount drawnCardAmount() {
        return amount;
    }
}
