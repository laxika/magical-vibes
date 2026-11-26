package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromControlledPermanentCost;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "19")
@CardRegistration(set = "PIP", collectorNumber = "368")
@CardRegistration(set = "PIP", collectorNumber = "547")
@CardRegistration(set = "PIP", collectorNumber = "896")
public class OverseerOfVault76 extends Card {

    public OverseerOfVault76() {
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(Triggers.CREATURE, new PermanentPowerAtMostPredicate(3))),
                        ObjectRelation.THIS_OR_ANOTHER_YOU_CONTROL),
                PutCountersEffect.onSelf(CounterType.QUEST));

        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                UnlessPaysEffect.youMayPay(new RemoveCounterFromControlledPermanentCost(
                                List.of(CounterType.QUEST), 3, new PermanentTruePredicate(), false),
                        new QueueReflexiveAbilityEffect(SequenceEffect.of(
                                PutCountersEffect.onEachYouControl(CounterType.PLUS_ONE_PLUS_ONE, 1, new PermanentIsCreaturePredicate()),
                                new GrantKeywordEffect(Keyword.VIGILANCE, Subjects.creaturesYouControl())))));
    }
}
