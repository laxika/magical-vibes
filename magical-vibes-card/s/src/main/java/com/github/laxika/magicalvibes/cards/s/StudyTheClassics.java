package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountersOnTargetPermanent;
import com.github.laxika.magicalvibes.model.effect.DoublePlusOneCountersOnTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TMC", collectorNumber = "128")
public class StudyTheClassics extends Card {

    public StudyTheClassics() {
        target(TargetFilters.creature())
                .addSpellEffect(new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1))
                .addSpellEffect(new DoublePlusOneCountersOnTargetCreatureEffect())
                .addSpellEffect(new GainLifeEffect(new CountersOnTargetPermanent(CounterType.PLUS_ONE_PLUS_ONE)));
    }
}
