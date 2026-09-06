package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "TLA", collectorNumber = "84")
public class AzulaAlwaysLies extends Card {

    public AzulaAlwaysLies() {
        var creatureFilter = new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature.");

        setAllowSharedTargets(true);
        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature gets -1/-1 until end of turn",
                        new BoostTargetCreatureEffect(-1, -1),
                        creatureFilter),
                new ChooseOneEffect.ChooseOneOption(
                        "Put a +1/+1 counter on target creature",
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE),
                        creatureFilter)
        )));
    }
}
