package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.TargetManaValue;

@CardRegistration(set = "LEG", collectorNumber = "16")
public class GreatDefender extends Card {

    public GreatDefender() {
        addSpellEffect(BoostEffect.target(new Fixed(0), new TargetManaValue()));
    }
}
