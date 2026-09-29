package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.StormEffect;
import com.github.laxika.magicalvibes.model.effect.TimeTravelEffect;

@CardRegistration(set = "WHO", collectorNumber = "34")
@CardRegistration(set = "WHO", collectorNumber = "352")
public class AllOfHistoryAllAtOnce extends Card {

    public AllOfHistoryAllAtOnce() {
        addEffect(EffectSlot.SPELL, new TimeTravelEffect(1));
        addEffect(EffectSlot.ON_SELF_CAST, new StormEffect());
    }
}
