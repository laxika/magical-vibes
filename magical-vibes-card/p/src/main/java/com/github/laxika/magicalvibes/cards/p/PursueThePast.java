package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "SOS", collectorNumber = "216")
public class PursueThePast extends Card {

    public PursueThePast() {
        addEffect(EffectSlot.SPELL, new GainLifeEffect(2));
        addEffect(EffectSlot.SPELL, new MayEffect(
                new DiscardAndDrawCardEffect(1, 2),
                "Discard a card to draw two cards?"
        ));
        addCastingOption(new FlashbackCast("{2}{R}{W}"));
    }
}
