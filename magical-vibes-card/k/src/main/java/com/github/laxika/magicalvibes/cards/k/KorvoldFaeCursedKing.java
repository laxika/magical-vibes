package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SLD", collectorNumber = "1625")
@CardRegistration(set = "EOC", collectorNumber = "120")
@CardRegistration(set = "ELD", collectorNumber = "329")
public class KorvoldFaeCursedKing extends Card {

    private static final SacrificePermanentThenEffect SACRIFICE_ANOTHER_PERMANENT =
            new SacrificePermanentThenEffect(
                    new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate()),
                    null,
                    "another permanent");

    private static final SequenceEffect SACRIFICE_TRIGGER = SequenceEffect.of(
            new PutCountersOnSourceEffect(1, 1, 1),
            new DrawCardEffect());

    public KorvoldFaeCursedKing() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), SACRIFICE_ANOTHER_PERMANENT);
        addTrigger(Triggers.attacks(), SACRIFICE_ANOTHER_PERMANENT);

        addTrigger(Triggers.sacrificed(null, ObjectRelation.YOU_CONTROL), SACRIFICE_TRIGGER);
    }
}
