package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.BuybackPaid;
import com.github.laxika.magicalvibes.model.effect.AdjustChosenCounterOnTargetEffect;
import com.github.laxika.magicalvibes.model.effect.BuybackEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;

@CardRegistration(set = "TSP", collectorNumber = "53")
public class Clockspinning extends Card {

    public Clockspinning() {
        addEffect(EffectSlot.STATIC, new BuybackEffect("{3}"));
        addEffect(EffectSlot.SPELL, new AdjustChosenCounterOnTargetEffect());
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new BuybackPaid(), ReturnToHandEffect.selfSpell()));
    }
}
