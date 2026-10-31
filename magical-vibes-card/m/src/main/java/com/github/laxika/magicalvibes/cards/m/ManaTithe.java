package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;

@CardRegistration(set = "PLC", collectorNumber = "25")
@CardRegistration(set = "MB1", collectorNumber = "171")
@CardRegistration(set = "TSR", collectorNumber = "26")
@CardRegistration(set = "STA", collectorNumber = "8")
public class ManaTithe extends Card {

    public ManaTithe() {
        addSpellEffect(new CounterUnlessPaysEffect(1));
    }
}
