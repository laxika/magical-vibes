package com.github.laxika.magicalvibes.model.effect;

/**
 * ETB effect that perpetually boosts other creatures the controller controls and creature cards
 * in that controller's hand that match the source permanent's chosen creature subtype.
 */
public record PerpetuallyBoostOtherOwnCreaturesAndHandCreatureCardsOfChosenSubtypeEffect(
        int powerBoost, int toughnessBoost) implements CardEffect {
}
