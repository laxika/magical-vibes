package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

/**
 * Asks the controller to choose a card name while the effect resolves and stores that name on
 * the source permanent.
 */
public record ChooseCardNameAtResolutionEffect(CardType requiredType) implements CardEffect {

    public ChooseCardNameAtResolutionEffect() {
        this(null);
    }
}
