package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LearnEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;

@CardRegistration(set = "STX", collectorNumber = "42")
public class DreamStrix extends Card {

    public DreamStrix() {
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_SPELL, new SacrificeSelfEffect());

        addEffect(EffectSlot.ON_DEATH, new LearnEffect());
    }
}
