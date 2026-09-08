package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;

import java.util.List;

@CardRegistration(set = "M21", collectorNumber = "118")
public class PestilentHaze extends Card {

    public PestilentHaze() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "All creatures get -2/-2 until end of turn",
                        new BoostAllCreaturesEffect(-2, -2)),
                new ChooseOneEffect.ChooseOneOption(
                        "Remove two loyalty counters from each planeswalker",
                        new RemoveCounterFromEachMatchingPermanentEffect(
                                CounterType.LOYALTY,
                                2,
                                new PermanentIsPlaneswalkerPredicate(),
                                EachPermanentScope.ALL_PLAYERS))
        )));
    }
}
