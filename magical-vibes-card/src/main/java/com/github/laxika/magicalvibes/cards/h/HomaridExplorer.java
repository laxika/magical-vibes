package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "DOM", collectorNumber = "53")
public class HomaridExplorer extends Card {

    public HomaridExplorer() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MillTargetPlayerEffect(4));
    }
}
