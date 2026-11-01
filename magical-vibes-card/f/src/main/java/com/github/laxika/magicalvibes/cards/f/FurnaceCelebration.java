package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOM", collectorNumber = "90")
@CardRegistration(set = "UMA", collectorNumber = "131")
@CardRegistration(set = "C13", collectorNumber = "109")
public class FurnaceCelebration extends Card {

    public FurnaceCelebration() {
        addTrigger(Triggers.sacrificed(null, ObjectRelation.ANOTHER_YOU_CONTROL),
                new TriggeringPermanentConditionalEffect(
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate()),
                new MayPayManaEffect(
                        "{2}", new DealDamageToAnyTargetEffect(2, false),
                        "Pay {2} to deal 2 damage to any target?"
                )));
    }
}
