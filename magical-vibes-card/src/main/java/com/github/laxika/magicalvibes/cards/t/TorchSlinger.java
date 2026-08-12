package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "ZEN", collectorNumber = "151")
public class TorchSlinger extends Card {

    public TorchSlinger() {
        addEffect(EffectSlot.STATIC, new KickerEffect("{1}{R}"));
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        )).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ConditionalEffect(new Kicked(), new DealDamageToTargetCreatureEffect(2)));
    }
}
