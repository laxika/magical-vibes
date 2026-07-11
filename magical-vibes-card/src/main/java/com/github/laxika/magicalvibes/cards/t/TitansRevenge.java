package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.ClashEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;

@CardRegistration(set = "MOR", collectorNumber = "110")
public class TitansRevenge extends Card {

    public TitansRevenge() {
        // Titan's Revenge deals X damage to any target.
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(new XValue()));
        // Clash with an opponent. If you win, return Titan's Revenge to its owner's hand.
        addEffect(EffectSlot.SPELL, new ClashEffect(ReturnToHandEffect.selfSpell()));
    }
}
