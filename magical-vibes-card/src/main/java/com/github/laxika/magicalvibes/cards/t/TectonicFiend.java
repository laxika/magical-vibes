package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterEchoAtNextUpkeepEffect;

@CardRegistration(set = "TSP", collectorNumber = "181")
public class TectonicFiend extends Card {

    public TectonicFiend() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new RegisterEchoAtNextUpkeepEffect("{4}{R}{R}"));
        addEffect(EffectSlot.STATIC, new MustAttackEffect());
    }
}
