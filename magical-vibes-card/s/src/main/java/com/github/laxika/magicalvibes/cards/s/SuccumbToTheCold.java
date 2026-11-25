package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "WOE", collectorNumber = "72")
public class SuccumbToTheCold extends Card {

    public SuccumbToTheCold() {
        target(TargetFilters.creatureAnOpponentControls(), 1, 2)
                .addSpellEffect(new TapPermanentsEffect(Subjects.target()))
                .addSpellEffect(new PutCountersEffect(CounterType.STUN));
    }
}
