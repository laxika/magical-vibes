package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

/** A controller-draw trigger whose payload depends on the card actually drawn. */
public interface DrawnCardTriggerEffect extends CardEffect {

    CardEffect effectForDrawnCard(Card drawnCard);
}
