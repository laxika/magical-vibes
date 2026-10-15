package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.SpellXAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "TLE", collectorNumber = "243")
public class RokusMastery extends Card {

    public RokusMastery() {
        addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(new XValue()));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new SpellXAtLeast(4), new ScryEffect(2)));
    }
}
