package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DrawAndLoseLifePerSubtypeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RIX", collectorNumber = "64")
@CardRegistration(set = "LCC", collectorNumber = "188")
@CardRegistration(set = "VOC", collectorNumber = "124")
public class ChampionOfDusk extends Card {

    public ChampionOfDusk() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DrawAndLoseLifePerSubtypeEffect(CardSubtype.VAMPIRE));
    }
}
