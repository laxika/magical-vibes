package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerEffect;

@CardRegistration(set = "ISD", collectorNumber = "54")
public class DreamTwist extends Card {

    public DreamTwist() {
        addEffect(EffectSlot.SPELL, new MillTargetPlayerEffect(3));
        addCastingOption(new FlashbackCast("{1}{U}"));
    }
}
