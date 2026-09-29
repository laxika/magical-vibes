package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.TargetSpellManaValue;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;

@CardRegistration(set = "PIP", collectorNumber = "104")
@CardRegistration(set = "PIP", collectorNumber = "414")
@CardRegistration(set = "PIP", collectorNumber = "632")
@CardRegistration(set = "PIP", collectorNumber = "942")
public class Electrosiphon extends Card {

    public Electrosiphon() {
        addEffect(EffectSlot.SPELL, new EnergyCountersEffect(new TargetSpellManaValue()));
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
