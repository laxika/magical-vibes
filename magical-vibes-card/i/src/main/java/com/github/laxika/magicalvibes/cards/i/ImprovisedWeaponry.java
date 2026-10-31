package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "AFR", collectorNumber = "150")
@CardRegistration(set = "HBG", collectorNumber = "184")
public class ImprovisedWeaponry extends Card {

    public ImprovisedWeaponry() {
        addSpellEffect(new DealDamageToAnyTargetEffect(2));
        addSpellEffect(CreateTokenEffect.ofTreasureToken(1));
    }
}
