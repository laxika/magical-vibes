package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageNotRemovedFromOpponentsCreaturesDuringCleanupEffect;

@CardRegistration(set = "YMID", collectorNumber = "29")
public class PatientZero extends Card {

    public PatientZero() {
        addEffect(EffectSlot.STATIC, new DamageNotRemovedFromOpponentsCreaturesDuringCleanupEffect());
    }
}
