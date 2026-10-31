package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "JUD", collectorNumber = "87")
public class EmberShot extends Card {

    public EmberShot() {
        addSpellEffect(new DealDamageToAnyTargetEffect(3));
        addSpellEffect(new DrawCardEffect(1));
    }
}
