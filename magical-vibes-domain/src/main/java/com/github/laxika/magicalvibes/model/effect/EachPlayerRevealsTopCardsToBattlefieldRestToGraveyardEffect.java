package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Each player reveals that many cards from the top of their library, puts permanent cards
 * revealed this way onto the battlefield, and puts the remaining cards into their graveyard.
 * The amount is evaluated separately for each player.
 */
public record EachPlayerRevealsTopCardsToBattlefieldRestToGraveyardEffect(
        DynamicAmount amount
) implements CardEffect {
}
