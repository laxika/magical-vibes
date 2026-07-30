package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "HML", collectorNumber = "41")
public class BaronSengir extends Card {

    public BaronSengir() {
        // Whenever a creature dealt damage by this creature this turn dies, put a +2/+2 counter on it.
        addEffect(EffectSlot.ON_DAMAGED_CREATURE_DIES, new PutCountersOnSelfEffect(CounterType.PLUS_TWO_PLUS_TWO));

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
