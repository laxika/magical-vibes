package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

/**
 * Triggered effect (ON_CONTROLLER_GAINS_LIFE) that deals damage to target creature or player
 * whenever a spell of the specified color causes the controller to gain life.
 * Only triggers when the life gain source is an instant or sorcery spell of the matching color.
 * Used by Firesong and Sunspeaker.
 */
public record DealDamageOnSpellLifeGainEffect(int damage, CardColor triggeringColor) implements CardEffect {
}
