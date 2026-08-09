package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventNextDamageByTargetCreatureEffect;

@CardRegistration(set = "MRD", collectorNumber = "6")
public class AweStrike extends Card {

    public AweStrike() {
        addEffect(EffectSlot.SPELL, new PreventNextDamageByTargetCreatureEffect());
    }
}
