package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsPermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "13")
public class ColleenWingStreetSamurai extends Card {

    public ColleenWingStreetSamurai() {
        addTrigger(Triggers.youCastSpell()
                        .withStackEntryFilter(new StackEntryTargetsPermanentPredicate(new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentControlledBySourceControllerPredicate())))),
                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE),
                new ScryEffect(1));
    }
}
