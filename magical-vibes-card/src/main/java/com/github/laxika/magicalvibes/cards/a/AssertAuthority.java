package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CounteredSpellDestination;

@CardRegistration(set = "MRD", collectorNumber = "30")
public class AssertAuthority extends Card {

    public AssertAuthority() {
        addEffect(EffectSlot.SPELL, new CounterSpellEffect(CounteredSpellDestination.EXILE));
    }
}
