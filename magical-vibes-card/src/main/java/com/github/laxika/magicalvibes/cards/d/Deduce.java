package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "MKM", collectorNumber = "52")
@CardRegistration(set = "MKM", collectorNumber = "293")
@CardRegistration(set = "SOA", collectorNumber = "16")
public class Deduce extends Card {

    public Deduce() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
        addEffect(EffectSlot.SPELL, CreateTokenEffect.ofClueToken(1));
    }
}
