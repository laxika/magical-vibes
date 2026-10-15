package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

@CardRegistration(set = "MSC", collectorNumber = "662")
public class MACH1SwoopingScoundrel extends Card {

    public MACH1SwoopingScoundrel() {
        OncePerTurnTriggerEffect surveil = new OncePerTurnTriggerEffect(new SurveilEffect(1));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, surveil);
        addEffect(EffectSlot.ON_CONTROLLER_GAINS_LIFE, surveil);
    }
}
