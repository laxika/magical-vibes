package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RampageEffect;

@CardRegistration(set = "CHR", collectorNumber = "79")
@CardRegistration(set = "LEG", collectorNumber = "244")
public class MarhaultElsdragon extends Card {

    public MarhaultElsdragon() {
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new RampageEffect(1));
    }
}
