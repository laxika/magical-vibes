package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles each creature the spell's controller's opponents control. Each affected creature's
 * controller searches their library for a basic land card for each creature exiled this way and
 * puts those cards onto the battlefield tapped, then shuffles.
 */
public record ExileAllCreaturesYouDontControlAndSearchBasicLandsToBattlefieldTappedEffect()
        implements CardEffect {
}
