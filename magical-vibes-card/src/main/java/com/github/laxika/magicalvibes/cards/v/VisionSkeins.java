package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;

@CardRegistration(set = "DIS", collectorNumber = "36")
public class VisionSkeins extends Card {

    public VisionSkeins() {
        addEffect(EffectSlot.SPELL, new EachPlayerDrawsCardEffect(2));
    }
}
