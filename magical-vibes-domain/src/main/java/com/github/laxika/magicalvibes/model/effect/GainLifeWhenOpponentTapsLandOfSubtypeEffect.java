package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * {@code Triggers.playerTapsLandForMana()} trigger-condition record: "Whenever an opponent taps a {subtype} for mana,
 * you may gain {lifeAmount} life." The land-tap collector judges it by class (the tapping player an opponent of the
 * source's controller, the tapped land of the subtype) and puts {@link #triggeredEffect()} on the stack, whose "you may"
 * is asked as it resolves (CR 603.5). Used by Sanctimony.
 */
public record GainLifeWhenOpponentTapsLandOfSubtypeEffect(CardSubtype subtype, int lifeAmount) implements CardEffect {

    /** "You may gain {lifeAmount} life." */
    public CardEffect triggeredEffect() {
        return new MayEffect(new GainLifeEffect(lifeAmount), "Gain " + lifeAmount + " life?");
    }
}
