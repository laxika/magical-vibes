package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Resolves Phabine's Parley ability: each player reveals the top card of their library, a Citizen
 * token is created for each land revealed, creatures controlled by the ability's controller get
 * +1/+1 for each nonland revealed until end of turn, and then each player draws a card.
 */
public record PhabineParleyEffect() implements CardDrawingEffect {

    @Override
    public DynamicAmount drawnCardAmount() {
        return new Fixed(1);
    }
}
