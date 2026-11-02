package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TapCreaturesBoostSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DSK", collectorNumber = "22")
public class OrphansOfTheWheat extends Card {

    public OrphansOfTheWheat() {
        addTrigger(Triggers.attacks(), new TapCreaturesBoostSelfEffect());
    }
}
