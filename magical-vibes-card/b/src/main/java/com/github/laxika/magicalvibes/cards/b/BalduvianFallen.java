package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;

import java.util.List;

@CardRegistration(set = "CSP", collectorNumber = "51")
public class BalduvianFallen extends Card {

    public BalduvianFallen() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, CumulativeUpkeepEffect.withPaidEffects(
                "{1}", List.of(new BoostSelfEffect(new XValue(), new Fixed(0)))));
    }
}
