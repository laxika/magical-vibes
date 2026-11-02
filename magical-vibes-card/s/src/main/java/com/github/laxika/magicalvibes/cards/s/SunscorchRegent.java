package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DTK", collectorNumber = "41")
@CardRegistration(set = "FIC", collectorNumber = "255")
@CardRegistration(set = "MOC", collectorNumber = "209")
@CardRegistration(set = "C21", collectorNumber = "107")
@CardRegistration(set = "BLC", collectorNumber = "158")
@CardRegistration(set = "C17", collectorNumber = "74")
public class SunscorchRegent extends Card {

    public SunscorchRegent() {
        addTrigger(Triggers.opponentCastsSpell(), new SpellCastTriggerEffect(
                null,
                List.of(
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),
                        new GainLifeEffect(1))));
    }
}
