package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;

@CardRegistration(set = "AVR", collectorNumber = "127")
public class BannersRaised extends Card {

    public BannersRaised() {
        addEffect(EffectSlot.SPELL, new BoostAllOwnCreaturesEffect(1, 0));
    }
}
