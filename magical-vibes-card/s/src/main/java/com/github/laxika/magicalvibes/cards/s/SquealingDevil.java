package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.ColorSpentToCast;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.EachPlayerPaysAnyManaEffect.Resource;
import com.github.laxika.magicalvibes.model.effect.EachPlayerPaysAnyManaEffect;

@CardRegistration(set = "DIS", collectorNumber = "72")
public class SquealingDevil extends Card {

    public SquealingDevil() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), SequenceEffect.of(
                EachPlayerPaysAnyManaEffect.byYou(Resource.X_MANA),
                new ConditionalEffect(new HappenedThisWay(StepKind.PAY_MANA),
                        BoostEffect.target(AmountThisWay.manaPaid(), new Fixed(0)))));
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ConditionalEffect(
                new NotCondition(new ColorSpentToCast(ManaColor.BLACK)), new SacrificeSelfEffect()));
    }
}
