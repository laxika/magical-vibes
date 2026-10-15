package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PlaysAdditionalLandEachTurnEffect;

@CardRegistration(set = "BLB", collectorNumber = "218")
public class HugsGrislyGuardian extends Card {

    public HugsGrislyGuardian() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ExileTopCardsMayPlayUntilNextTurnEffect(new XValue()));
        addEffect(EffectSlot.STATIC, new PlaysAdditionalLandEachTurnEffect(1));
    }
}
