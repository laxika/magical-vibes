package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;

@CardRegistration(set = "TLE", collectorNumber = "96")
@CardRegistration(set = "TLE", collectorNumber = "181")
public class TuiAndLaMoonAndOcean extends Card {

    public TuiAndLaMoonAndOcean() {
        addEffect(EffectSlot.ON_ALLY_PERMANENT_BECOMES_TAPPED,
                new TriggeringPermanentConditionalEffect(
                        new PermanentIsSourceCardPredicate(), new DrawCardEffect(1)));
        addEffect(EffectSlot.ON_SELF_BECOMES_UNTAPPED, new PutCountersOnSourceEffect(1, 1, 1));
    }
}
