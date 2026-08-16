package com.github.laxika.magicalvibes.model.effect;

import java.util.Optional;

/**
 * A controller-draw trigger whose payload is selected from the number of cards drawn this turn.
 * The draw service unwraps this marker before putting the payload on the stack.
 */
public interface DrawTriggerEffect extends CardEffect {

    Optional<CardEffect> effectForDrawCount(int cardsDrawnThisTurn);
}
