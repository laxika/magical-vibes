package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.ChosenPermanentPower;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TapCreatureCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "EOE", collectorNumber = "43")
public class WedgelightRammer extends Card {

    public WedgelightRammer() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect("Robot", 2, 2, null,
                List.of(CardSubtype.ROBOT), Set.of(), Set.of(CardType.ARTIFACT)));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new TapCreatureCost(new PermanentIsCreaturePredicate(), true, true),
                        new PutCountersOnSelfEffect(CounterType.CHARGE, new ChosenPermanentPower())
                ),
                "Tap another untapped creature you control: Put charge counters equal to its power "
                        + "on Wedgelight Rammer.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));

        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceCounterThreshold(9, CounterType.CHARGE),
                new AnimatePermanentsEffect(
                        (DynamicAmount) null, (DynamicAmount) null, List.of(), Set.of(), null,
                        Set.of(CardType.CREATURE), GrantScope.SELF, EffectDuration.CONTINUOUS, null
                )));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceCounterThreshold(9, CounterType.CHARGE),
                new GrantKeywordEffect(Set.of(Keyword.FLYING, Keyword.FIRST_STRIKE), GrantScope.SELF)));
    }
}
