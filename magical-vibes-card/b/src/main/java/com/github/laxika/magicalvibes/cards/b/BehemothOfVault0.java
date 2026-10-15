package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.TargetManaValue;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PayEnergyThenDestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "PIP", collectorNumber = "127")
@CardRegistration(set = "PIP", collectorNumber = "655")
public class BehemothOfVault0 extends Card {

    public BehemothOfVault0() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnergyCountersEffect(4));

        target(TargetFilters.nonlandPermanent()).addEffect(EffectSlot.ON_DEATH,
                new MayEffect(
                        new PayEnergyThenDestroyTargetPermanentEffect(new TargetManaValue()),
                        "Pay energy equal to that permanent's mana value to destroy it?"));
    }
}
