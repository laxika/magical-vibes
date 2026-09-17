package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomeStartingPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "MB1", collectorNumber = "55")
public class ImpatientIguana extends Card {

    public ImpatientIguana() {
        addEffect(EffectSlot.ON_OPENING_HAND_REVEAL, new MayEffect(
                new BecomeStartingPlayerEffect(),
                "Become the starting player?"));
    }
}
