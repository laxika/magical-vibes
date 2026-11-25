package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "VOW", collectorNumber = "138")
@CardRegistration(set = "DBL", collectorNumber = "405")
public class WeddingSecurity extends Card {

    public WeddingSecurity() {
        addTrigger(Triggers.attacks(), new MayEffect(
                SequenceEffect.of(new SacrificePermanentsEffect(1, new PermanentAllOfPredicate(List.of(
                                new PermanentIsTokenPredicate(),
                                new PermanentHasSubtypePredicate(CardSubtype.BLOOD))), Subjects.you()),
                                new ConditionalEffect(HappenedThisWay.sacrificed(), SequenceEffect.of(
                                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE),
                                new DrawCardEffect()))),
                "Sacrifice a Blood token?"));
    }
}
