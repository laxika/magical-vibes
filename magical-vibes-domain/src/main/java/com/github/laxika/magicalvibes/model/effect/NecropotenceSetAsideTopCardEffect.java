package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * One-shot effect: "Exile the top card of your library face down. Put that card into your hand at
 * the beginning of your next end step." Exiles the controller's top library card and registers a
 * {@code ReturnExiledCardToHandAtEndStep} delayed action that returns it to hand at the controller's
 * next end step. Used by Necropotence.
 *
 * <p>Implements {@link CardDrawingEffect} (draws one card, delayed) so the AI reads it as card advantage.
 */
public record NecropotenceSetAsideTopCardEffect() implements CardDrawingEffect {

    @Override
    public DynamicAmount drawnCardAmount() {
        return new Fixed(1);
    }
}
