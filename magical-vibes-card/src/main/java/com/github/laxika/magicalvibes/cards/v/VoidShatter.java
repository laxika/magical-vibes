package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CounteredSpellDestination;

@CardRegistration(set = "OGW", collectorNumber = "49")
public class VoidShatter extends Card {

    public VoidShatter() {
        addEffect(EffectSlot.SPELL, new CounterSpellEffect(CounteredSpellDestination.EXILE));
    }
}
