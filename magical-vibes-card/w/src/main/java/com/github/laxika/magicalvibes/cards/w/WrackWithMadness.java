package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureDealsPowerDamageToSelfEffect;

@CardRegistration(set = "DKA", collectorNumber = "107")
public class WrackWithMadness extends Card {

    public WrackWithMadness() {
        addEffect(EffectSlot.SPELL, new TargetCreatureDealsPowerDamageToSelfEffect());
    }
}
