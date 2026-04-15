package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.EachOpponentMillsEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerEffect;

@CardRegistration(set = "DKA", collectorNumber = "32")
public class ChillOfForeboding extends Card {

    public ChillOfForeboding() {
        addEffect(EffectSlot.SPELL, new MillControllerEffect(5));
        addEffect(EffectSlot.SPELL, new EachOpponentMillsEffect(5));
        addCastingOption(new FlashbackCast("{7}{U}"));
    }
}
