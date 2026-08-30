package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.HarmonizeCast;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "TDM", collectorNumber = "62")
public class UnendingWhisper extends Card {

    public UnendingWhisper() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
        addCastingOption(new HarmonizeCast("{5}{U}"));
    }
}
