package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterEchoAtNextUpkeepEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "USG", collectorNumber = "179")
@CardRegistration(set = "VMA", collectorNumber = "157")
@CardRegistration(set = "DDL", collectorNumber = "56")
@CardRegistration(set = "EMA", collectorNumber = "124")
@CardRegistration(set = "C13", collectorNumber = "104")
public class CraterHellion extends Card {

    public CraterHellion() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new MassDamageEffect(4, false, false,
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate())));
        addStaticEffect(new RegisterEchoAtNextUpkeepEffect("{4}{R}{R}"));
    }
}
