package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "BFZ", collectorNumber = "75")
public class DampeningPulse extends Card {

    public DampeningPulse() {
        addStaticEffect(new StaticBoostEffect(-1, 0, Subjects.creaturesYourOpponentsControl()));
    }
}
