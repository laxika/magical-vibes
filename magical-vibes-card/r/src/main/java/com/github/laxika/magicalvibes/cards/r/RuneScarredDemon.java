package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M12", collectorNumber = "106")
@CardRegistration(set = "FDN", collectorNumber = "184")
@CardRegistration(set = "IMA", collectorNumber = "106")
@CardRegistration(set = "CMM", collectorNumber = "184")
@CardRegistration(set = "CMM", collectorNumber = "521")
public class RuneScarredDemon extends Card {

    public RuneScarredDemon() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new SearchLibraryEffect());
    }
}
