package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureDealsPowerDamageToSelfEffect;

@CardRegistration(set = "TMP", collectorNumber = "37")
@CardRegistration(set = "TPR", collectorNumber = "25")
public class Repentance extends Card {

    public Repentance() {
        addEffect(EffectSlot.SPELL, new TargetCreatureDealsPowerDamageToSelfEffect());
    }
}
