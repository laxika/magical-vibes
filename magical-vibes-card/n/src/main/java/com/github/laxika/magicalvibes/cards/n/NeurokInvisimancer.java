package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOM", collectorNumber = "37")
@CardRegistration(set = "DDI", collectorNumber = "8")
public class NeurokInvisimancer extends Card {

    public NeurokInvisimancer() {
        addStaticEffect(new CantBeBlockedEffect());
        target(TargetFilters.creature())
                .addTrigger(Triggers.enters(ObjectRelation.THIS), new MakeCreatureUnblockableEffect());
    }
}
