package com.github.laxika.magicalvibes.model.effect;

/**
 * Separates all creature cards in the controller's graveyard into two piles. An opponent chooses
 * one pile to exile, and the other pile returns to the battlefield under the controller's control.
 */
public record DeathOrGloryEffect() implements CardEffect {
}
