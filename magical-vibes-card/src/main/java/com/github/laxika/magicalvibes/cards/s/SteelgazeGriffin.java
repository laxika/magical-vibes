package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "ELD", collectorNumber = "65")
public class SteelgazeGriffin extends Card {

    public SteelgazeGriffin() {
        addEffect(EffectSlot.ON_CONTROLLER_DRAWS_SECOND_CARD, new BoostSelfEffect(2, 0));
    }
}
