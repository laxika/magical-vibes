package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

@CardRegistration(set = "MRD", collectorNumber = "204")
public class MesmericOrb extends Card {

    public MesmericOrb() {
        addEffect(EffectSlot.ON_ANY_PERMANENT_BECOMES_UNTAPPED,
                new MillEffect(1, MillRecipient.UNTAPPED_PERMANENT_CONTROLLER));
    }
}
