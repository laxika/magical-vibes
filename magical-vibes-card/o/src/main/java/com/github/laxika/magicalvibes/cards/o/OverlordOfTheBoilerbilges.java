package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.CastForAlternateCost;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCardTypeFromAttachedPermanentEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DSK", collectorNumber = "146")
public class OverlordOfTheBoilerbilges extends Card {

    public OverlordOfTheBoilerbilges() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{2}{R}{R}"))));

        DealDamageToAnyTargetEffect damage = new DealDamageToAnyTargetEffect(4);
        addTrigger(Triggers.enters(ObjectRelation.THIS), damage);
        addTrigger(Triggers.attacks(), damage);

        addStaticEffect(new ConditionalEffect(
                new CastForAlternateCost(),
                new EnterWithCountersEffect(CounterType.TIME, new Fixed(4))));
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new AllOf(List.of(
                                new CastForAlternateCost(),
                                new SourceCounterThreshold(1, CounterType.TIME)))),
                new RemoveCounterFromSourceEffect(CounterType.TIME, 1));
        addStaticEffect(new ConditionalEffect(
                new AllOf(List.of(
                        new CastForAlternateCost(),
                        new SourceCounterThreshold(1, CounterType.TIME))),
                new RemoveCardTypeFromAttachedPermanentEffect(CardType.CREATURE, GrantScope.SELF)));
    }
}
