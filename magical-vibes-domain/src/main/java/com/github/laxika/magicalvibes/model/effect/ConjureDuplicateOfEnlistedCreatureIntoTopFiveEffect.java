package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

/** Conjures a frozen duplicate of the enlisted creature into a random position among the top five. */
public record ConjureDuplicateOfEnlistedCreatureIntoTopFiveEffect(Card creatureCard) implements CardEffect {
}
