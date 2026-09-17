package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CounteredSpellDestination;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "HOC", collectorNumber = "17")
@CardRegistration(set = "HOC", collectorNumber = "57")
public class Reprieve extends Card {

    public Reprieve() {
        addEffect(EffectSlot.SPELL, new CounterSpellEffect(CounteredSpellDestination.HAND));
        addEffect(EffectSlot.SPELL, new DrawCardEffect());
    }
}
