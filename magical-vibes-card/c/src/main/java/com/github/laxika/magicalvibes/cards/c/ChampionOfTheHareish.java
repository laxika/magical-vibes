package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BuddyListGrowOrWriteEffect;
import com.github.laxika.magicalvibes.model.effect.BuddyListOnEnterEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MB2", collectorNumber = "275")
@CardRegistration(set = "MB2", collectorNumber = "511")
public class ChampionOfTheHareish extends Card {

    public ChampionOfTheHareish() {
        addStaticEffect(new BuddyListOnEnterEffect(List.of(CardSubtype.RABBIT, CardSubtype.SOLDIER)));
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                new BuddyListGrowOrWriteEffect());
    }
}
