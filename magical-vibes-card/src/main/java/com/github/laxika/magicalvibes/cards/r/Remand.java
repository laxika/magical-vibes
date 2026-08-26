package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CounteredSpellDestination;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "RAV", collectorNumber = "63")
public class Remand extends Card {

    public Remand() {
        addEffect(EffectSlot.SPELL, new CounterSpellEffect(CounteredSpellDestination.HAND));
        addEffect(EffectSlot.SPELL, new DrawCardEffect());
    }
}
