package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "5ED", collectorNumber = "219")
public class Disintegrate extends Card {

    public Disintegrate() {
        // X damage to any target; a creature dealt damage this way can't be regenerated
        // this turn, and if it would die this turn it is exiled instead.
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(new XValue(), true, true));
    }
}
