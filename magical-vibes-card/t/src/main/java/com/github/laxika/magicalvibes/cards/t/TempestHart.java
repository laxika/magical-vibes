package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.s.ScanTheClouds;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;

import java.util.List;

@CardRegistration(set = "WOE", collectorNumber = "238")
public class TempestHart extends Card {

    public TempestHart() {
        setBackFaceCard(new ScanTheClouds());
        addCastingOption(new AdventureCast("{1}{U}"));

        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardMinManaValuePredicate(5),
                List.of(new PutCountersOnSourceEffect(1, 1, 1))
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "ScanTheClouds";
    }
}
