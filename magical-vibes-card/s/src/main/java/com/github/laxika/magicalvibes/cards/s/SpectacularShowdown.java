package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.condition.Overloaded;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.GoadTriggeringCreatureUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachMatchingPermanentThenGoadEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnTargetPermanentThenReflexiveEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MKC", collectorNumber = "162")
public class SpectacularShowdown extends Card {

    public SpectacularShowdown() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{4}{R}{R}{R}"))));
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new Overloaded(),
                new PutCountersOnTargetPermanentThenReflexiveEffect(
                        CounterType.DOUBLE_STRIKE, 1, null,
                        new GoadTriggeringCreatureUntilNextTurnEffect(), false, null, true),
                new PutCounterOnEachMatchingPermanentThenGoadEffect(
                        CounterType.DOUBLE_STRIKE, 1, new PermanentIsCreaturePredicate(),
                        EachPermanentScope.ALL_PLAYERS)));
        target(TargetFilters.creature());
    }
}
