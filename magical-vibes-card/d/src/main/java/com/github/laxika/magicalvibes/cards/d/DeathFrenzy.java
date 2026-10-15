package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedCreatureDeathTriggerEffect;

@CardRegistration(set = "KTK", collectorNumber = "172")
public class DeathFrenzy extends Card {

    public DeathFrenzy() {
        addEffect(EffectSlot.SPELL, new BoostAllCreaturesEffect(-2, -2));
        addEffect(EffectSlot.SPELL, new RegisterDelayedCreatureDeathTriggerEffect(new GainLifeEffect(1)));
    }
}
