package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventArtifactDamageToSelfEffect;

@CardRegistration(set = "ATQ", collectorNumber = "30")
public class ArgothianTreefolk extends Card {

    public ArgothianTreefolk() {
        addEffect(EffectSlot.STATIC, new PreventArtifactDamageToSelfEffect());
    }
}
