package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

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
        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(
                        null,
                        SequenceEffect.of(
                                new PutCountersOnSelfEffect(CounterType.QUEST),
                                CreateTokenEffect.ofFoodToken(1)),
                        false,
                        true));

        addEffect(EffectSlot.ON_ALLY_PERMANENT_SACRIFICED,
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.FOOD),
                        new BoostTargetCreatureEffect(
                                new CountersOnSource(CounterType.QUEST),
                                new CountersOnSource(CounterType.QUEST),
                                CREATURE_YOU_CONTROL)));
    }
}
