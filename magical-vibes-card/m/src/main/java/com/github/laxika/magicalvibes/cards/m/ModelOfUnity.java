package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.ModelOfUnityEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LTC", collectorNumber = "78")
@CardRegistration(set = "LTC", collectorNumber = "158")
@CardRegistration(set = "LTC", collectorNumber = "488")
public class ModelOfUnity extends Card {

    public ModelOfUnity() {
        addTrigger(Triggers.playersFinishVoting(), new ModelOfUnityEffect());
        addActivatedAbility(ManaAbilities.tapForAnyColor());
    }
}
