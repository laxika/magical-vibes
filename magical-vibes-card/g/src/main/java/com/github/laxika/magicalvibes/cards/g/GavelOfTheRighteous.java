package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EquipEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "NCC", collectorNumber = "83")
@CardRegistration(set = "NCC", collectorNumber = "183")
@CardRegistration(set = "EOC", collectorNumber = "55")
public class GavelOfTheRighteous extends Card {

    public GavelOfTheRighteous() {
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                new PutCountersOnSelfEffect(CounterType.CHARGE));

        CountersOnSource counters = new CountersOnSource(CounterType.ANY);
        addEffect(EffectSlot.STATIC,
                new AttachedBoostEffect(counters, counters, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceCounterThreshold(4, CounterType.ANY),
                new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.EQUIPPED_CREATURE)));

        addActivatedAbility(new EquipActivatedAbility("{3}"));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new RemoveCounterFromSourceCost(), new EquipEffect()),
                "Equip — Remove a counter from Gavel of the Righteous",
                new ControlledPermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature you control"),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
