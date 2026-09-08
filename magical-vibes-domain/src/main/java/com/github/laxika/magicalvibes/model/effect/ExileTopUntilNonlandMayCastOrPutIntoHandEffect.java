package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.LifeGainedThisTurn;

/**
 * Exiles cards from the controller's library until a nonland card is found, then offers that card
 * as a free cast when its mana value is at most {@code maxManaValue}; otherwise it goes to hand.
 */
public record ExileTopUntilNonlandMayCastOrPutIntoHandEffect(DynamicAmount maxManaValue)
        implements CardEffect {

    public ExileTopUntilNonlandMayCastOrPutIntoHandEffect() {
        this(new LifeGainedThisTurn(CountScope.CONTROLLER));
    }
}
