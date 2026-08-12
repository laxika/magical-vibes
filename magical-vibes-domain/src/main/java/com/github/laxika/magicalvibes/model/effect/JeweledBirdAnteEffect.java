package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Jeweled Bird's activated ability: ante the source artifact, put the controller's other anted
 * cards into their owner's graveyard, then draw a card.
 *
 * <p>The engine represents the ante zone through exile and tracks which exile entries came from
 * an ante action so ordinary exiled cards are not affected.
 */
public record JeweledBirdAnteEffect() implements CardDrawingEffect {

    @Override
    public DynamicAmount drawnCardAmount() {
        return new Fixed(1);
    }
}
