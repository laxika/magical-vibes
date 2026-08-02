package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;

@CardRegistration(set = "GTC", collectorNumber = "144")
public class AureliasFury extends Card {

    public AureliasFury() {
        // Aurelia's Fury deals X damage divided as you choose among any number of targets. Tap each
        // creature dealt damage this way. Players dealt damage this way can't cast noncreature
        // spells this turn.
        addEffect(EffectSlot.SPELL, DealDividedDamageEffect.xAmongAnyTargetsTapAndLockNoncreature());
    }
}
