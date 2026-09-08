package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement effect: if a source controlled by this permanent's controller would deal
 * damage to an opponent, prevent that damage and mill that many cards from each opponent.
 */
public record PreventDamageToOpponentsAndMillEffect()
        implements ControllerOpponentDamageMillReplacementEffect {
}
