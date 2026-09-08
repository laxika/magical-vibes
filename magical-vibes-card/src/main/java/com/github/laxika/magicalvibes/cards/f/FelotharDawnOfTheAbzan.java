package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "TDM", collectorNumber = "184")
public class FelotharDawnOfTheAbzan extends Card {

    public FelotharDawnOfTheAbzan() {
        CardEffect sacrificeThenCounters = new MayEffect(
                new SacrificePermanentThenEffect(
                        new PermanentNotPredicate(new PermanentIsLandPredicate()),
                        new PutCounterOnEachControlledPermanentEffect(
                                CounterType.PLUS_ONE_PLUS_ONE, 1, new PermanentIsCreaturePredicate()),
                        "a nonland permanent"),
                "Sacrifice a nonland permanent?");
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, sacrificeThenCounters);
        addEffect(EffectSlot.ON_ATTACK, sacrificeThenCounters);
    }
}
