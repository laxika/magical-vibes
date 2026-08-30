package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardRandomCardThenMassDamageEffect;

@CardRegistration(set = "DIS", collectorNumber = "66")
public class KindleTheCarnage extends Card {

    public KindleTheCarnage() {
        addEffect(EffectSlot.SPELL, new DiscardRandomCardThenMassDamageEffect());
    }
}
