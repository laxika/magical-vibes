package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MH1", collectorNumber = "133")
public class IgneousElemental extends Card {

    public IgneousElemental() {
        addStaticEffect(new ConditionalEffect(
                new GraveyardCardThreshold(1, new CardTypePredicate(CardType.LAND)),
                new ReduceOwnCastCostEffect(new Fixed(2))));

        target(TargetFilters.creature()).addTrigger(Triggers.enters(ObjectRelation.THIS),
                new MayEffect(new DealDamageToTargetCreatureEffect(2),
                        "Deal 2 damage to target creature?"));
    }
}
