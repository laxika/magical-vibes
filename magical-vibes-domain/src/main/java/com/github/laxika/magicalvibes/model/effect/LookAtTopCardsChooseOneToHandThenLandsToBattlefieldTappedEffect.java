package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Looks at the top cards of the controller's library, offers one card for the hand, then offers
 * any number of the remaining land cards for the battlefield tapped and puts the rest into the
 * graveyard.
 */
public record LookAtTopCardsChooseOneToHandThenLandsToBattlefieldTappedEffect(
        DynamicAmount count) implements CardEffect {
}
