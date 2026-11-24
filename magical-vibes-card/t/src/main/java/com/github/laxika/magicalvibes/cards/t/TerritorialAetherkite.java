package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.EachPlayerPaysAnyManaEffect.Resource;
import com.github.laxika.magicalvibes.model.effect.EachPlayerPaysAnyManaEffect;

@CardRegistration(set = "DRC", collectorNumber = "12")
@CardRegistration(set = "DRC", collectorNumber = "28")
public class TerritorialAetherkite extends Card {

    public TerritorialAetherkite() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                SequenceEffect.of(
                        new EnergyCountersEffect(2),
                        EachPlayerPaysAnyManaEffect.byYou(Resource.ENERGY),
                        new ConditionalEffect(new HappenedThisWay(StepKind.PAY_ENERGY),
                                QueueReflexiveAbilityEffect.withEventValue(
                                        new MassDamageEffect(new EventValue(), false, false,
                                                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())),
                                        AmountThisWay.energyPaid()))));
    }
}
