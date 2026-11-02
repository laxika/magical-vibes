package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExploreEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "11")
@CardRegistration(set = "WHO", collectorNumber = "334")
@CardRegistration(set = "WHO", collectorNumber = "616")
@CardRegistration(set = "WHO", collectorNumber = "925")
public class AstridPeth extends Card {

    public AstridPeth() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), CreateTokenEffect.ofFoodToken(1));
        addTrigger(Triggers.attacks(), CreateTokenEffect.ofFoodToken(1));
        addTrigger(Triggers.sacrificed(
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentHasSubtypePredicate(CardSubtype.CLUE),
                                new PermanentHasSubtypePredicate(CardSubtype.FOOD))),
                        ObjectRelation.YOU_CONTROL),
                new ExploreEffect());
    }
}
