package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CounterSpellIfManaValueEqualsSourceCountersEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;

@CardRegistration(set = "MRD", collectorNumber = "150")
public class ChaliceOfTheVoid extends Card {

    public ChaliceOfTheVoid() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.CHARGE, new XValue()));
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL,
                new CounterSpellIfManaValueEqualsSourceCountersEffect(CounterType.CHARGE));
    }
}
