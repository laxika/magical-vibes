package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerXEffect;

@CardRegistration(set = "DKA", collectorNumber = "41")
public class IncreasingConfusion extends Card {

    public IncreasingConfusion() {
        addEffect(EffectSlot.SPELL, new MillTargetPlayerXEffect(2));
        addCastingOption(new FlashbackCast("{X}{U}"));
    }
}
