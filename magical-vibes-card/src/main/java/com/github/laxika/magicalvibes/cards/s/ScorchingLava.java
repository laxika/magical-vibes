package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;

@CardRegistration(set = "INV", collectorNumber = "164")
public class ScorchingLava extends Card {

    public ScorchingLava() {
        addEffect(EffectSlot.STATIC, new KickerEffect("{R}"));
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new Kicked(),
                new DealDamageToAnyTargetEffect(new Fixed(2)),
                new DealDamageToAnyTargetEffect(new Fixed(2), true, true)
        ));
    }
}
