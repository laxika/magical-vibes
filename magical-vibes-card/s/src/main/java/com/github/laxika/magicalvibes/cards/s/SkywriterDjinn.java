package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ConjureSkywriterDjinnSpellbookEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YDMU", collectorNumber = "5")
public class SkywriterDjinn extends Card {

    public SkywriterDjinn() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ConjureSkywriterDjinnSpellbookEffect());
    }
}
