package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerByHandSizeEffect;

@CardRegistration(set = "10E", collectorNumber = "241")
public class SuddenImpact extends Card {

    public SuddenImpact() {
        setNeedsTarget(true);
        addEffect(EffectSlot.SPELL, new DealDamageToTargetPlayerByHandSizeEffect());
    }
}
