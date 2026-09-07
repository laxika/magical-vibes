package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.AdjustChosenCounterOnTargetEffect;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "257")
public class JhoirasTimebug extends Card {

    public JhoirasTimebug() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AdjustChosenCounterOnTargetEffect(true, CounterType.TIME)),
                "{T}: Choose target permanent you control or suspended card you own. If that permanent or card has a time counter on it, you may remove a time counter from it or put another time counter on it."));
    }
}
