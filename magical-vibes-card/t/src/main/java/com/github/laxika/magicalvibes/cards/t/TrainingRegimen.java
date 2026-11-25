package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSH", collectorNumber = "192")
public class TrainingRegimen extends Card {

    public TrainingRegimen() {
        addStaticEffect(new GrantKeywordEffect(
                Keyword.TRAMPLE,
                Subjects.creaturesYouControl(),
                new PermanentHasCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE)
        ));

        target(TargetFilters.creatureYouControl()).addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                new PutCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, 1));
    }
}
