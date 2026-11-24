package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveUpToCountersFromAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

import java.util.List;

@CardRegistration(set = "NPH", collectorNumber = "137")
public class HexParasite extends Card {

    public HexParasite() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{X}{B/P}",
                List.of(
                        new RemoveUpToCountersFromAllPermanentsEffect(CounterType.ANY, new XValue(), Subjects.target()),
                        new BoostSelfEffect(new AmountThisWay(StepKind.REMOVE_COUNTERS), new Fixed(0))),
                "{X}{B/P}: Remove up to X counters from target permanent. For each counter removed this way, Hex Parasite gets +1/+0 until end of turn."
        ));
    }
}
