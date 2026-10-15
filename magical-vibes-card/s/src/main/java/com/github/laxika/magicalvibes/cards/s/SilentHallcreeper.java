package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreaturePermanentlyEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseModeNotYetChosenEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "DSK", collectorNumber = "72")
public class SilentHallcreeper extends Card {

    public SilentHallcreeper() {
        addEffect(EffectSlot.STATIC, new CantBeBlockedEffect());

        ControlledPermanentPredicateTargetFilter anotherCreatureYouControl =
                new ControlledPermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentNotPredicate(new PermanentIsSourceCardPredicate()))),
                        "Target must be another creature you control");
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new ChooseModeNotYetChosenEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Put two +1/+1 counters on Silent Hallcreeper",
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 2)),
                new ChooseOneEffect.ChooseOneOption("Draw a card", new DrawCardEffect()),
                new ChooseOneEffect.ChooseOneOption(
                        "Silent Hallcreeper becomes a copy of another target creature you control",
                        new BecomeCopyOfTargetCreaturePermanentlyEffect(), anotherCreatureYouControl))));
    }
}
