package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M21", collectorNumber = "85")
@CardRegistration(set = "WAR", collectorNumber = "75")
@CardRegistration(set = "ANB", collectorNumber = "37")
public class WallOfRunes extends Card {

    public WallOfRunes() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ScryEffect(1));
    }
}
