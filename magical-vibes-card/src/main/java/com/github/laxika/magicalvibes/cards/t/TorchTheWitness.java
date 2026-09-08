package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.EventValueAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MKM", collectorNumber = "146")
public class TorchTheWitness extends Card {

    public TorchTheWitness() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL,
                        new DealDamageToTargetCreatureEffect(new Scaled(new XValue(), 2)))
                .addEffect(EffectSlot.SPELL,
                        new ConditionalEffect(new EventValueAtLeast(1), CreateTokenEffect.ofClueToken(1)));
    }
}
