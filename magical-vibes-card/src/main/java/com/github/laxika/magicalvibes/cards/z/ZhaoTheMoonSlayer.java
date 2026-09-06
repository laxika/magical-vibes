package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnterPermanentsOfTypesTappedEffect;
import com.github.laxika.magicalvibes.model.effect.NonbasicLandsBecomeTypeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "TLA", collectorNumber = "162")
public class ZhaoTheMoonSlayer extends Card {

    public ZhaoTheMoonSlayer() {
        var nonbasicLand = new PermanentAllOfPredicate(List.of(
                new PermanentIsLandPredicate(),
                new PermanentNotPredicate(new PermanentHasSupertypePredicate(CardSupertype.BASIC))
        ));

        addEffect(EffectSlot.STATIC, EnterPermanentsOfTypesTappedEffect.matching(nonbasicLand, false));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{7}",
                List.of(new PutCountersOnSelfEffect(CounterType.CONQUEROR)),
                "{7}: Put a conqueror counter on Zhao."
        ));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceCounterThreshold(1, CounterType.CONQUEROR),
                new NonbasicLandsBecomeTypeEffect(CardSubtype.MOUNTAIN)
        ));
    }
}
