package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.ConjureCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YMID", collectorNumber = "6")
public class ExpeditionSupplier extends Card {

    public ExpeditionSupplier() {
        // "Whenever Expedition Supplier and/or one or more other Humans and/or Warriors you control enter":
        // one ability, which "triggers only once each turn".
        addTrigger(new TriggerCondition(Triggers.enters(new PermanentAllOfPredicate(List.of(Triggers.CREATURE,
                                new PermanentAnyOfPredicate(List.of(new PermanentHasSubtypePredicate(CardSubtype.HUMAN),
                                        new PermanentHasSubtypePredicate(CardSubtype.WARRIOR))))),
                        ObjectRelation.THIS_OR_ANOTHER_YOU_CONTROL)),
                TriggerOptions.ONE_OR_MORE.withLimit(TriggerLimit.ONCE_EACH_TURN),
                new ConjureCardToBattlefieldEffect("ZNR", "256"));
    }
}
