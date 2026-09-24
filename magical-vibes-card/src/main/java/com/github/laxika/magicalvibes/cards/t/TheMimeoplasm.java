package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MimeoplasmCopyOnEnterEffect;

@CardRegistration(set = "SLD", collectorNumber = "136")
@CardRegistration(set = "SLD", collectorNumber = "1773")
public class TheMimeoplasm extends Card {

    public TheMimeoplasm() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MimeoplasmCopyOnEnterEffect());
    }
}
