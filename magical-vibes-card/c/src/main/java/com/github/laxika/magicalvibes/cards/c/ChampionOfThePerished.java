package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MID", collectorNumber = "91")
@CardRegistration(set = "SLD", collectorNumber = "837")
@CardRegistration(set = "DBL", collectorNumber = "91")
@CardRegistration(set = "SCD", collectorNumber = "71")
@CardRegistration(set = "FDC", collectorNumber = "91")
public class ChampionOfThePerished extends Card {

    public ChampionOfThePerished() {
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                new TriggeringCardConditionalEffect(new CardSubtypePredicate(CardSubtype.ZOMBIE),
                        new PutCountersOnSourceEffect(1, 1, 1)));
    }
}
