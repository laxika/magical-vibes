package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandEffect;

@CardRegistration(set = "ISD", collectorNumber = "63")
public class LostInTheMist extends Card {

    public LostInTheMist() {
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
        addEffect(EffectSlot.SPELL, new ReturnTargetPermanentToHandEffect());
    }
}
