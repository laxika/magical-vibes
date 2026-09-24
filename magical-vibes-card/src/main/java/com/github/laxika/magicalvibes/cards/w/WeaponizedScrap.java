package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.UpgradeEffect;

@CardRegistration(set = "MB1", collectorNumber = "111")
public class WeaponizedScrap extends Card {

    public WeaponizedScrap() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new UpgradeEffect());
    }
}
