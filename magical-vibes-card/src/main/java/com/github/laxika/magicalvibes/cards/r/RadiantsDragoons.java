package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterEchoAtNextUpkeepEffect;

@CardRegistration(set = "ULG", collectorNumber = "21")
public class RadiantsDragoons extends Card {

    public RadiantsDragoons() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifeEffect(5));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new RegisterEchoAtNextUpkeepEffect("{3}{W}"));
    }
}
