package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.UntapTargetPermanentEffect;

@CardRegistration(set = "SOM", collectorNumber = "53")
public class VolitionReins extends Card {

    public VolitionReins() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new UntapTargetPermanentEffect());
        addEffect(EffectSlot.STATIC, new ControlEnchantedCreatureEffect());
    }
}
