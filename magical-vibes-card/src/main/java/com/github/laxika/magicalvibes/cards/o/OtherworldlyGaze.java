package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

@CardRegistration(set = "MID", collectorNumber = "67")
public class OtherworldlyGaze extends Card {

    public OtherworldlyGaze() {
        addEffect(EffectSlot.SPELL, new SurveilEffect(3));
        addCastingOption(new FlashbackCast("{1}{U}"));
    }
}
