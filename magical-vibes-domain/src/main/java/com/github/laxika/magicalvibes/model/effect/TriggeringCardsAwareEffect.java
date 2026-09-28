package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

import java.util.List;

/** Allows a trigger collector to bind the cards involved in an event to an effect. */
public interface TriggeringCardsAwareEffect {

    CardEffect withTriggeringCards(List<Card> cards);
}
