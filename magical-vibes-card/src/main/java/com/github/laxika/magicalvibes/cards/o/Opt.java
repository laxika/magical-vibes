package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "XLN", collectorNumber = "65")
@CardRegistration(set = "DOM", collectorNumber = "60")
public class Opt extends Card {

    public Opt() {
        addEffect(EffectSlot.SPELL, new ScryEffect(1));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
    }
}
