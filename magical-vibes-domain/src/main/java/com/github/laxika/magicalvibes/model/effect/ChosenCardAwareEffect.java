package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

/** A hand-choice follow-up whose effect data depends on the card selected by that choice. */
public interface ChosenCardAwareEffect {

    CardEffect withChosenCard(Card card);
}
