package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;

@CardRegistration(set = "INV", collectorNumber = "178")
public class UrzasRage extends Card {

    public UrzasRage() {
        addEffect(EffectSlot.STATIC, new KickerEffect("{8}{R}"));
        addEffect(EffectSlot.STATIC, new CantBeCounteredEffect());
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new Kicked(),
                new DealDamageToAnyTargetEffect(new Fixed(3)),
                new DealDamageToAnyTargetEffect(new Fixed(10), new Kicked())
        ));
    }
}
