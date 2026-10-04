package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToToughnessEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "XLN", collectorNumber = "213")
@CardRegistration(set = "LCC", collectorNumber = "262")
@CardRegistration(set = "C21", collectorNumber = "208")
@CardRegistration(set = "FDC", collectorNumber = "239")
public class VerdantSunsAvatar extends Card {

    public VerdantSunsAvatar() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifeEqualToToughnessEffect());
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD, new GainLifeEqualToToughnessEffect());
    }
}
