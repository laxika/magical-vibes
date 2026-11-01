package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "HML", collectorNumber = "41")
@CardRegistration(set = "ME1", collectorNumber = "58")
public class BaronSengir extends Card {

    public BaronSengir() {
        // Whenever a creature dealt damage by this creature this turn dies, put a +2/+2 counter on it.
        addTrigger(Triggers.creatureDies(ObjectRelation.DAMAGED_BY_THIS),
                new PutCountersOnSelfEffect(CounterType.PLUS_TWO_PLUS_TWO));

        // {T}: Regenerate another target Vampire.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new RegenerateEffect(true)),
                "{T}: Regenerate another target Vampire.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentHasSubtypePredicate(CardSubtype.VAMPIRE),
                                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
                        )),
                        "Target must be another Vampire"
                )
        ));
    }
}
