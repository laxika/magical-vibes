package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ErestorOfTheCouncilEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LTC", collectorNumber = "53")
@CardRegistration(set = "LTC", collectorNumber = "136")
@CardRegistration(set = "LTC", collectorNumber = "457")
public class ErestorOfTheCouncil extends Card {

    public ErestorOfTheCouncil() {
        addTrigger(Triggers.playersFinishVoting(), new ErestorOfTheCouncilEffect());
    }
}
