package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ELD", collectorNumber = "116")
public class BrimstoneTrebuchet extends Card {

    public BrimstoneTrebuchet() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(DealDamageEffect.to(Subjects.eachOpponent(), 1)),
                "{T}: This creature deals 1 damage to each opponent."
        ));

        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.CREATURE,
                                new PermanentHasSubtypePredicate(CardSubtype.KNIGHT))),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                new UntapPermanentsEffect(Subjects.source()));
    }
}
