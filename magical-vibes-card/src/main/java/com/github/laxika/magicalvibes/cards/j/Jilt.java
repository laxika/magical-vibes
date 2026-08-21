package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "APC", collectorNumber = "27")
public class Jilt extends Card {

    public Jilt() {
        addEffect(EffectSlot.STATIC, new KickerEffect("{1}{R}"));
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL, ReturnToHandEffect.target());
        targetWhenKicked(TargetFilters.creature(), 0, 0, 1, 1)
                .addEffect(EffectSlot.SPELL,
                        new ConditionalEffect(new Kicked(), new DealDamageToTargetCreatureEffect(2)));
    }
}
