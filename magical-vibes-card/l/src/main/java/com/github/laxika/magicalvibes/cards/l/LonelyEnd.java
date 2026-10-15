package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerWasNotStartingPlayer;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCountersFromTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "YBRO", collectorNumber = "6")
public class LonelyEnd extends Card {

    public LonelyEnd() {
        var planeswalker = new PermanentIsPlaneswalkerPredicate();

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature gets -3/-3 until end of turn",
                        new BoostTargetCreatureEffect(-3, -3), TargetFilters.creature()),
                new ChooseOneEffect.ChooseOneOption(
                        "Remove three loyalty counters from target planeswalker",
                        new RemoveCountersFromTargetPermanentEffect(CounterType.LOYALTY, 3, planeswalker),
                        new PermanentPredicateTargetFilter(
                                planeswalker, "Target must be a planeswalker."))
        )));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new ControllerWasNotStartingPlayer(), new GainLifeEffect(3)));
    }
}
