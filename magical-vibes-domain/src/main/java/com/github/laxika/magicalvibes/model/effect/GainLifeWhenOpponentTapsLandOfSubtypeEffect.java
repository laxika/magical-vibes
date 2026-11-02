package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * {@code Triggers.playerTapsLandForMana()} trigger: "Whenever an opponent taps a {subtype} for mana, you may gain
 * {lifeAmount} life." Fires only when the tapping player is an opponent of the source's controller
 * and the tapped land has the given subtype. The "may" is auto-accepted (life gain has no downside),
 * consistent with the immediate land-tap trigger framework. Used by Sanctimony.
 */
public record GainLifeWhenOpponentTapsLandOfSubtypeEffect(CardSubtype subtype, int lifeAmount) implements CardEffect {
}
