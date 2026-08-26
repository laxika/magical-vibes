package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.KioraSovereignOfTheDeepTriggerEffect;

@CardRegistration(set = "MAT", collectorNumber = "35")
public class KioraSovereignOfTheDeep extends Card {

    public KioraSovereignOfTheDeep() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new KioraSovereignOfTheDeepTriggerEffect());
    }
}
