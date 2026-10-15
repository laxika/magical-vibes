package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawFromAnywhereInLibraryEffect;

@CardRegistration(set = "MB2", collectorNumber = "294")
@CardRegistration(set = "MB2", collectorNumber = "530")
public class HeartOfADuelist extends Card {

    public HeartOfADuelist() {
        addEffect(EffectSlot.STATIC, new DrawFromAnywhereInLibraryEffect());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect());
    }
}
