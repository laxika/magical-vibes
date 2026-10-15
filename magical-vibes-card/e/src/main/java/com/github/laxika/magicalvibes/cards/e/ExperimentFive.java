package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PayMulticoloredSourceManaCost;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;

@CardRegistration(set = "MB1", collectorNumber = "73")
public class ExperimentFive extends Card {

    public ExperimentFive() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(
                        new PayMulticoloredSourceManaCost(),
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_TWO)),
                "{1}{Z}: Put a +1/+2 counter on Experiment Five. ({Z} is paid with one mana from any source that could produce two or more colors of mana.)"));
    }
}
