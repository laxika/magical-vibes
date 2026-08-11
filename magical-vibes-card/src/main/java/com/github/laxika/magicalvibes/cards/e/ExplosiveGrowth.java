package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;

@CardRegistration(set = "INV", collectorNumber = "187")
public class ExplosiveGrowth extends Card {

    public ExplosiveGrowth() {
        addEffect(EffectSlot.STATIC, new KickerEffect("{5}"));
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new Kicked(),
                new BoostTargetCreatureEffect(2, 2),
                new BoostTargetCreatureEffect(5, 5)
        ));
    }
}
