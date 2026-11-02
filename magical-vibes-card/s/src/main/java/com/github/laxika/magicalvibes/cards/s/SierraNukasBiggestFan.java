package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "25")
@CardRegistration(set = "PIP", collectorNumber = "372")
@CardRegistration(set = "PIP", collectorNumber = "553")
@CardRegistration(set = "PIP", collectorNumber = "900")
public class SierraNukasBiggestFan extends Card {

    private static final PermanentPredicate CREATURE_YOU_CONTROL = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentControlledBySourceControllerPredicate()));

    public SierraNukasBiggestFan() {
        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer(),
                new AllyCombatDamageTriggerEffect(
                        null,
                        SequenceEffect.of(
                                new PutCountersOnSelfEffect(CounterType.QUEST),
                                CreateTokenEffect.ofFoodToken(1)),
                        false,
                        true));

        addTrigger(Triggers.sacrificed(new PermanentHasSubtypePredicate(CardSubtype.FOOD), ObjectRelation.YOU_CONTROL),
                new BoostTargetCreatureEffect(
                        new CountersOnSource(CounterType.QUEST),
                        new CountersOnSource(CounterType.QUEST),
                        CREATURE_YOU_CONTROL));
    }
}
