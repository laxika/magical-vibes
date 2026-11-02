package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AradeshTheFounderEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PIP", collectorNumber = "9")
@CardRegistration(set = "PIP", collectorNumber = "362")
@CardRegistration(set = "PIP", collectorNumber = "890")
@CardRegistration(set = "PIP", collectorNumber = "537")
public class AradeshTheFounder extends Card {

    public AradeshTheFounder() {
        addTrigger(Triggers.attackingCreatureYouControlAbilityTriggers(), new AradeshTheFounderEffect());
    }
}
