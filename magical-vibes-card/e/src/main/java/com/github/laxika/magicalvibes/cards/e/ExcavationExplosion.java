package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "BRO", collectorNumber = "132")
public class ExcavationExplosion extends Card {

    public ExcavationExplosion() {
        addSpellEffect(new DealDamageToAnyTargetEffect(3));
        addSpellEffect(CreateTokenEffect.ofPowerstoneToken(new Fixed(1)));
    }
}
