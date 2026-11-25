package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;

@CardRegistration(set = "BRO", collectorNumber = "132")
public class ExcavationExplosion extends Card {

    public ExcavationExplosion() {
        addSpellEffect(DealDamageEffect.toAnyTarget(3));
        addSpellEffect(CreateTokenEffect.ofPowerstoneToken(new Fixed(1)));
    }
}
