package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.PutCreatureFromHandWithManaValueLessThanTriggeringPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

@CardRegistration(set = "MAT", collectorNumber = "16")
public class ArniMetalbrow extends Card {

    public ArniMetalbrow() {
        MayPayManaEffect ability = new MayPayManaEffect(
                "{1}{R}",
                new PutCreatureFromHandWithManaValueLessThanTriggeringPermanentEffect(),
                "Put a creature card from your hand onto the battlefield tapped and attacking?",
                true
        );
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS, ability);
        addEffect(EffectSlot.ON_SELF_OR_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringPermanentConditionalEffect(new PermanentIsAttackingPredicate(), ability));
    }
}
