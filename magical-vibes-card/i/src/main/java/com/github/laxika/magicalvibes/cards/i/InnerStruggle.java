package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureDealsPowerDamageToSelfEffect;

@CardRegistration(set = "SOI", collectorNumber = "167")
public class InnerStruggle extends Card {

    public InnerStruggle() {
        addEffect(EffectSlot.SPELL, new TargetCreatureDealsPowerDamageToSelfEffect());
    }
}
