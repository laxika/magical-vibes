package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.condition.ControlledCommanderAsCast;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MoveCounterFromTargetCreatureToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersFromTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;

import java.util.List;

@CardRegistration(set = "SOC", collectorNumber = "21")
@CardRegistration(set = "SOC", collectorNumber = "71")
public class NexusMentality extends Card {

    public NexusMentality() {
        TargetFilter nonlandPermanentYouControl = new ControlledPermanentPredicateTargetFilter(
                new PermanentNotPredicate(new PermanentIsLandPredicate()),
                "Target must be a nonland permanent you control");

        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMoreWhen(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Move all counters from target nonland permanent you control onto another target nonland permanent you control",
                        List.of(new MoveCounterFromTargetCreatureToTargetCreatureEffect(true)),
                        List.<TargetFilter>of(nonlandPermanentYouControl, nonlandPermanentYouControl)),
                new ChooseOneEffect.ChooseOneOption(
                        "Remove all counters from target nonland permanent you control. Draw a card for each counter removed this way",
                        List.of(new RemoveAllCountersFromTargetPermanentEffect(),
                                new DrawCardEffect(new EventValue())),
                        nonlandPermanentYouControl)
        ), new ControlledCommanderAsCast()));
    }
}
