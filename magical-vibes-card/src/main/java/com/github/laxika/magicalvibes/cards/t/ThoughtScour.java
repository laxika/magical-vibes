package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerEffect;

@CardRegistration(set = "DKA", collectorNumber = "52")
public class ThoughtScour extends Card {

    public ThoughtScour() {
        addEffect(EffectSlot.SPELL, new MillTargetPlayerEffect(2));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
    }
}
