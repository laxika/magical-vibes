package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMaySearchLibraryForBasicLandsToMatchMostLandsEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "C21", collectorNumber = "22")
public class ScholarshipSponsor extends Card {

    public ScholarshipSponsor() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new EachPlayerMaySearchLibraryForBasicLandsToMatchMostLandsEffect());
    }
}
