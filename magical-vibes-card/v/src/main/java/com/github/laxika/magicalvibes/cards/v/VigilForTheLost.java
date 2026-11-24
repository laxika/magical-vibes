package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.EachPlayerPaysAnyManaEffect.Resource;
import com.github.laxika.magicalvibes.model.effect.EachPlayerPaysAnyManaEffect;

@CardRegistration(set = "SOM", collectorNumber = "26")
public class VigilForTheLost extends Card {

    public VigilForTheLost() {
        addTrigger(Triggers.creatureDies(ObjectRelation.YOU_CONTROL), SequenceEffect.of(
                EachPlayerPaysAnyManaEffect.byYou(Resource.X_MANA),
                new ConditionalEffect(new HappenedThisWay(StepKind.PAY_MANA),
                        new GainLifeEffect(AmountThisWay.manaPaid()))));
    }
}
