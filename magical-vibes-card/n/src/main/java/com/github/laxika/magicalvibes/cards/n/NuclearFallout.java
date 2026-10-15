package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GiveEachPlayerRadCounterEffect;

@CardRegistration(set = "PIP", collectorNumber = "47")
@CardRegistration(set = "PIP", collectorNumber = "331")
@CardRegistration(set = "PIP", collectorNumber = "575")
@CardRegistration(set = "PIP", collectorNumber = "859")
public class NuclearFallout extends Card {

    public NuclearFallout() {
        addEffect(EffectSlot.SPELL, new BoostAllCreaturesEffect(
                new Scaled(new XValue(), -2), new Scaled(new XValue(), -2)));
        addEffect(EffectSlot.SPELL, new GiveEachPlayerRadCounterEffect(new XValue()));
    }
}
