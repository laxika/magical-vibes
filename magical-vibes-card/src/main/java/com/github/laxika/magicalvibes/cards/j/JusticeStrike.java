package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureDealsPowerDamageToSelfEffect;

@CardRegistration(set = "GRN", collectorNumber = "182")
public class JusticeStrike extends Card {

    public JusticeStrike() {
        addEffect(EffectSlot.SPELL, new TargetCreatureDealsPowerDamageToSelfEffect());
    }
}
