package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.UUID;

/**
 * Resolution-time offer marker for Cache Grab. Accepting the offer returns the selected card and
 * may create the Food follow-up when the Squirrel condition is met.
 */
public record ReturnMilledPermanentToHandAndCreateFoodIfSquirrelEffect(
        UUID groupId, CardPredicate filter, CreateTokenEffect foodEffect, Card sourceCard) implements CardEffect {

    public ReturnMilledPermanentToHandAndCreateFoodIfSquirrelEffect(
            UUID groupId, CreateTokenEffect foodEffect, Card sourceCard) {
        this(groupId, new CardIsPermanentPredicate(), foodEffect, sourceCard);
    }
}
