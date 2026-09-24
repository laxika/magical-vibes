package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.PayXLifeCost;

@CardRegistration(set = "EMA", collectorNumber = "108")
@CardRegistration(set = "SLD", collectorNumber = "1860")
@CardRegistration(set = "2XM", collectorNumber = "110")
@CardRegistration(set = "SOC", collectorNumber = "120")
@CardRegistration(set = "MSC", collectorNumber = "161")
@CardRegistration(set = "MSC", collectorNumber = "354")
@CardRegistration(set = "C13", collectorNumber = "96")
@CardRegistration(set = "CMM", collectorNumber = "191")
@CardRegistration(set = "CMM", collectorNumber = "523")
public class ToxicDeluge extends Card {

    public ToxicDeluge() {
        addEffect(EffectSlot.SPELL, new PayXLifeCost());
        addEffect(EffectSlot.SPELL, new BoostAllCreaturesEffect(
                new Scaled(new XValue(), -1), new Scaled(new XValue(), -1)));
    }
}
