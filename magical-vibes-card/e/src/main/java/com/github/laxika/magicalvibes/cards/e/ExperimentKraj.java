package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.GainActivatedAbilitiesOfOwnCreaturesEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "DIS", collectorNumber = "110")
@CardRegistration(set = "GK2", collectorNumber = "117")
@CardRegistration(set = "CMM", collectorNumber = "337")
@CardRegistration(set = "CMM", collectorNumber = "578")
public class ExperimentKraj extends Card {

    public ExperimentKraj() {
        addStaticEffect(GainActivatedAbilitiesOfOwnCreaturesEffect.of(Subjects.otherCreatures(),
                new PermanentHasCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE)));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE)),
                "{T}: Put a +1/+1 counter on target creature.",
                TargetFilters.creature()
        ));
    }
}
