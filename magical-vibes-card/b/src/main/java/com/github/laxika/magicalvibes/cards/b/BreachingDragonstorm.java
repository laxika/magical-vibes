package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ExileTopUntilNonlandMayCastOrPutIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TDM", collectorNumber = "101")
@CardRegistration(set = "FDC", collectorNumber = "141")
public class BreachingDragonstorm extends Card {

    public BreachingDragonstorm() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ExileTopUntilNonlandMayCastOrPutIntoHandEffect(new Fixed(8)));
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.CREATURE,
                                new PermanentHasSubtypePredicate(CardSubtype.DRAGON))),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                ReturnToHandEffect.self());
    }
}
