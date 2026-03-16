package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.EachPlayerChoosesCreatureDestroyRestEffect;

@CardRegistration(set = "ISD", collectorNumber = "10")
public class DivineReckoning extends Card {

    public DivineReckoning() {
        addEffect(EffectSlot.SPELL, new EachPlayerChoosesCreatureDestroyRestEffect());
        addCastingOption(new FlashbackCast("{5}{W}{W}"));
    }
}
