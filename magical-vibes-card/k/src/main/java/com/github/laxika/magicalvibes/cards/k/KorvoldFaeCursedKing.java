package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SLD", collectorNumber = "1625")
@CardRegistration(set = "EOC", collectorNumber = "120")
@CardRegistration(set = "ELD", collectorNumber = "329")
public class KorvoldFaeCursedKing extends Card {

    private static final CardEffect SACRIFICE_ANOTHER_PERMANENT =
            new SacrificePermanentsEffect(1, new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate()), Subjects.you());

    private static final SequenceEffect SACRIFICE_TRIGGER = SequenceEffect.of(
            PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE),
            new DrawCardEffect());

    public KorvoldFaeCursedKing() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), SACRIFICE_ANOTHER_PERMANENT);
        addTrigger(Triggers.attacks(), SACRIFICE_ANOTHER_PERMANENT);

        addTrigger(Triggers.sacrificed(null, ObjectRelation.YOU_CONTROL), SACRIFICE_TRIGGER);
    }
}
