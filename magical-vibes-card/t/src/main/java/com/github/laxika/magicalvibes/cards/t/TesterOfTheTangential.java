package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.EachPlayerPaysAnyManaEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerPaysAnyManaEffect.Resource;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MoveCounterFromSourceToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOS", collectorNumber = "69")
public class TesterOfTheTangential extends Card {

    public TesterOfTheTangential() {
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                new MayEffect(SequenceEffect.of(
                        EachPlayerPaysAnyManaEffect.byYou(Resource.X_MANA),
                        QueueReflexiveAbilityEffect.withEventValue(
                                MoveCounterFromSourceToTargetCreatureEffect.ontoAnotherCreature(
                                        CounterType.PLUS_ONE_PLUS_ONE, new EventValue()),
                                AmountThisWay.manaPaid())),
                        "Pay {X} to move X +1/+1 counters onto another target creature?"));
    }
}
