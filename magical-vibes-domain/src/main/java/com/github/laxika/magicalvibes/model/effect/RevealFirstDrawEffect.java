package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;

/**
 * Reveals the first card the controller draws on each of their turns and selects one effect based
 * on whether it is a land. The selected effect is handled by the normal draw-trigger pipeline.
 */
public record RevealFirstDrawEffect(CardEffect onLand, CardEffect onNonland)
        implements FirstDrawRevealTriggerEffect {

    @Override
    public CardEffect effectFor(Card drawnCard) {
        return drawnCard.hasType(CardType.LAND) ? onLand : onNonland;
    }

    @Override
    public boolean onlyOnControllerTurn() {
        return true;
    }
}
