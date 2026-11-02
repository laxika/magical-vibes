package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.ExploreEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LCI", collectorNumber = "196")
@CardRegistration(set = "LCI", collectorNumber = "382")
@CardRegistration(set = "LCI", collectorNumber = "403")
public class JadelightSpelunker extends Card {

    public JadelightSpelunker() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ExploreEffect(new XValue()));
    }
}
