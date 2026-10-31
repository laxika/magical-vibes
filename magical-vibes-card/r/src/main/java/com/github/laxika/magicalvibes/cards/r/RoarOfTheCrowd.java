package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEqualToChosenTypeCountEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicates;

@CardRegistration(set = "MOR", collectorNumber = "100")
public class RoarOfTheCrowd extends Card {

    public RoarOfTheCrowd() {
        addSpellEffect(new DealDamageEqualToChosenTypeCountEffect(TargetPredicates.anyTarget()));
    }
}
