package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CounteredSpellDestination;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "ISD", collectorNumber = "53")
@CardRegistration(set = "MIR", collectorNumber = "61")
@CardRegistration(set = "M15", collectorNumber = "51")
@CardRegistration(set = "MID", collectorNumber = "49")
public class Dissipate extends Card {

    public Dissipate() {
        addEffect(EffectSlot.SPELL, new CounterSpellEffect(CounteredSpellDestination.EXILE));
    }
}
