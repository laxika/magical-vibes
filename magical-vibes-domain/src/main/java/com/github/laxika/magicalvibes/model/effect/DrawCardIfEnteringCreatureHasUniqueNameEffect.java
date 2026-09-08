package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

import java.util.UUID;

/**
 * Draws a card when the creature that caused an enter-the-battlefield trigger has no same-named
 * creature among the controller's creatures or creature cards in that player's graveyard.
 *
 * <p>The trigger collector fills in the entering card's name and id so the condition can be
 * checked again when the ability resolves.
 */
public record DrawCardIfEnteringCreatureHasUniqueNameEffect(String enteringCardName, UUID enteringCardId)
        implements CardDrawingEffect {

    public DrawCardIfEnteringCreatureHasUniqueNameEffect() {
        this(null, null);
    }

    @Override
    public DynamicAmount drawnCardAmount() {
        return new Fixed(1);
    }
}
