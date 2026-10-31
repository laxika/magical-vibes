package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.SpellXAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "TLE", collectorNumber = "243")
public class RokusMastery extends Card {

    public RokusMastery() {
        addSpellEffect(new DealDamageToTargetCreatureEffect(new XValue()));
        addSpellEffect(new ConditionalEffect(new SpellXAtLeast(4), new ScryEffect(2)));
    }
}
