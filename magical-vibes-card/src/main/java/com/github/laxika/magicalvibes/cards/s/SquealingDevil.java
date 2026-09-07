package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.ColorSpentToCast;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PayXManaBoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;

@CardRegistration(set = "DIS", collectorNumber = "72")
public class SquealingDevil extends Card {

    public SquealingDevil() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new PayXManaBoostTargetCreatureEffect());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, ConditionalEffect.unless(
                new NotCondition(new ColorSpentToCast(ManaColor.BLACK)), new SacrificeSelfEffect()));
    }
}
