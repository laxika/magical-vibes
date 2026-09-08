package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EndureEffect;

@CardRegistration(set = "TDM", collectorNumber = "83")
public class KinTreeNurturer extends Card {

    public KinTreeNurturer() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EndureEffect(1));
    }
}
