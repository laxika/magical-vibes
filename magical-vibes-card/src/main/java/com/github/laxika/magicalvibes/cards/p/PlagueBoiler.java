package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenEffect;
import com.github.laxika.magicalvibes.model.effect.StateTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAtLeastCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "RAV", collectorNumber = "269")
public class PlagueBoiler extends Card {

    public PlagueBoiler() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new PutCountersOnSelfEffect(CounterType.PLAGUE));

        addEffect(EffectSlot.STATE_TRIGGERED, new StateTriggerEffect(
                new PermanentHasAtLeastCountersPredicate(CounterType.PLAGUE, 3),
                List.of(new SacrificeSelfThenEffect(new DestroyAllPermanentsEffect(
                        new PermanentNotPredicate(new PermanentIsLandPredicate())))),
                "Plague Boiler's state-triggered ability"));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}{G}",
                List.of(new ChooseOneEffect(List.of(
                        new ChooseOneEffect.ChooseOneOption(
                                "Put a plague counter on Plague Boiler",
                                new PutCountersOnSelfEffect(CounterType.PLAGUE)),
                        new ChooseOneEffect.ChooseOneOption(
                                "Remove a plague counter from Plague Boiler",
                                new RemoveCounterFromSourceEffect(CounterType.PLAGUE, 1))))),
                "{1}{B}{G}: Put a plague counter on Plague Boiler or remove a plague counter from it."
        ));
    }
}
