package com.github.laxika.magicalvibes.model.effect;

/**
 * Chooses one creature card from an opponent's graveyard, then lets that card's owner choose one
 * creature card from the controller's graveyard. The controller may return the chosen cards under
 * their owners' control.
 */
public record DawnbreakReclaimerEffect() implements CardEffect {
}
