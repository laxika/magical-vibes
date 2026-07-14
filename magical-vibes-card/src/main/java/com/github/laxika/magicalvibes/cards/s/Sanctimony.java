package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeWhenOpponentTapsLandOfSubtypeEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "8ED", collectorNumber = "42")
@CardRegistration(set = "7ED", collectorNumber = "39")
public class Sanctimony extends Card {

    public Sanctimony() {
        // Whenever an opponent taps a Mountain for mana, you may gain 1 life.
        addEffect(EffectSlot.ON_ANY_PLAYER_TAPS_LAND,
                new GainLifeWhenOpponentTapsLandOfSubtypeEffect(CardSubtype.MOUNTAIN, 1));
    }
}
