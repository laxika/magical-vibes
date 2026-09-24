package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomeMonarchEffect;

@CardRegistration(set = "CMM", collectorNumber = "48")
public class PalaceSentinels extends Card {

    public PalaceSentinels() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BecomeMonarchEffect());
    }
}
