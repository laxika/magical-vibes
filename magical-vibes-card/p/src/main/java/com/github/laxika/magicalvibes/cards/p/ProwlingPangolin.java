package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AnyPlayerMaySacrificeCreaturesThenSacrificeSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ONS", collectorNumber = "163")
@CardRegistration(set = "EMA", collectorNumber = "104")
public class ProwlingPangolin extends Card {

    public ProwlingPangolin() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new AnyPlayerMaySacrificeCreaturesThenSacrificeSourceEffect(2));
    }
}
