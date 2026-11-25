package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ExileSourceCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;

@CardRegistration(set = "TLA", collectorNumber = "239")
public class SandbenderScavengers extends Card {

    public SandbenderScavengers() {
        addTrigger(Triggers.sacrificed(
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate()),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE));
        addTrigger(Triggers.dies(ObjectRelation.THIS), new MayEffect(
                SequenceEffect.of(new ExileSourceCardFromGraveyardEffect(),
                        new ConditionalEffect(HappenedThisWay.exiled(),
                                new QueueReflexiveAbilityEffect(ReturnCardFromGraveyardEffect.builder()
                                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                        .filter(new CardTypePredicate(CardType.CREATURE))
                                        .dynamicMaxManaValue(new SourcePower(true))
                                        .targetGraveyard(true)
                                        .build()))),
                "Exile Sandbender Scavengers?"));
    }
}
