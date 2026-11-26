package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;

@CardRegistration(set = "KLD", collectorNumber = "115")
@CardRegistration(set = "KLR", collectorNumber = "126")
public class FuriousReprisal extends Card {

    public FuriousReprisal() {
        target(2, 2).addSpellEffect(DealDamageEffect.toEachTarget(new Fixed(2)));
    }
}
