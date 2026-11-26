package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.condition.Overloaded;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.GoadTargetCreatureUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MKC", collectorNumber = "162")
public class SpectacularShowdown extends Card {

    public SpectacularShowdown() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{4}{R}{R}{R}"))));
        addSpellEffect(new ConditionalReplacementEffect(
                new Overloaded(),
                SequenceEffect.of(
                        new PutCountersEffect(CounterType.DOUBLE_STRIKE, 1),
                        GoadTargetCreatureUntilNextTurnEffect.ofCreaturesGivenCountersThisWay()),
                SequenceEffect.of(
                        PutCountersEffect.onEach(CounterType.DOUBLE_STRIKE, 1, new PermanentIsCreaturePredicate()),
                        GoadTargetCreatureUntilNextTurnEffect.ofCreaturesGivenCountersThisWay())));
        target(TargetFilters.creature());
    }
}
