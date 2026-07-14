package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Capability interface for effects that make the controller draw a single evaluated number of
 * cards. Lets consumers — chiefly the AI evaluators/classifiers — ask "how many cards does this
 * draw" without knowing the concrete effect type, mirroring how {@link ManaProducingEffect}
 * abstracts mana production.
 *
 * <p>Descriptive only: it states a fact drawn from the record's existing components, never a score.
 */
public interface CardDrawingEffect extends CardEffect {

    /**
     * The number of cards drawn, as a {@link DynamicAmount} evaluated at resolution (fixed number,
     * X paid, "for each …", …).
     */
    DynamicAmount drawnCardAmount();
}
