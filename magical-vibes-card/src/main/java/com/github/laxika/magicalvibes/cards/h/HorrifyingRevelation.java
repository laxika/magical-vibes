package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDiscardsEffect;

@CardRegistration(set = "MBS", collectorNumber = "45")
public class HorrifyingRevelation extends Card {

    public HorrifyingRevelation() {
        addEffect(EffectSlot.SPELL, new TargetPlayerDiscardsEffect(1));
        addEffect(EffectSlot.SPELL, new MillTargetPlayerEffect(1));
    }
}
