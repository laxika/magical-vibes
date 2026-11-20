package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RIX", collectorNumber = "64")
@CardRegistration(set = "LCC", collectorNumber = "188")
@CardRegistration(set = "VOC", collectorNumber = "124")
public class ChampionOfDusk extends Card {

    public ChampionOfDusk() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), SequenceEffect.of(
                new DrawCardEffect(new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.VAMPIRE), CountScope.CONTROLLER)),
                new LoseLifeEffect(new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.VAMPIRE), CountScope.CONTROLLER), Subjects.you())));
    }
}
