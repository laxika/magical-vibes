package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MOM", collectorNumber = "337")
@CardRegistration(set = "MOM", collectorNumber = "380")
@CardRegistration(set = "FDC", collectorNumber = "231")
public class SurrakAndGoreclaw extends Card {

    public SurrakAndGoreclaw() {
        addStaticEffect(new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.OWN_CREATURES));
        addTrigger(Triggers.enters(Triggers.NONTOKEN_CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                new PutCountersOnEnteringCreatureEffect(1, false,
                        List.of(new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET))));
    }
}
