package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantLoseGameEffect;

@CardRegistration(set = "10E", collectorNumber = "339")
@CardRegistration(set = "M10", collectorNumber = "218")
@CardRegistration(set = "M11", collectorNumber = "212")
@CardRegistration(set = "MRD", collectorNumber = "228")
@CardRegistration(set = "TD2", collectorNumber = "1")
@CardRegistration(set = "MPS", collectorNumber = "46")
@CardRegistration(set = "V15", collectorNumber = "13")
public class PlatinumAngel extends Card {

    public PlatinumAngel() {
        addEffect(EffectSlot.STATIC, new CantLoseGameEffect());
    }
}
