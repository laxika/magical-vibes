package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.s.ScanTheClouds;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WOE", collectorNumber = "238")
public class TempestHart extends Card {

    public TempestHart() {
        setBackFaceCard(new ScanTheClouds());
        addCastingOption(new AdventureCast("{1}{U}"));

        addTrigger(Triggers.youCastSpell().withFilter(new CardMinManaValuePredicate(5)),
                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE));
    }

    @Override
    public String getBackFaceClassName() {
        return "ScanTheClouds";
    }
}
