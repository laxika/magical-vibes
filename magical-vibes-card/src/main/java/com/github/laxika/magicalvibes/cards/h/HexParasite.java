package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RemoveCountersFromTargetAndBoostSelfEffect;

import java.util.List;

@CardRegistration(set = "NPH", collectorNumber = "137")
public class HexParasite extends Card {

    public HexParasite() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{X}{B/P}",
                List.of(new RemoveCountersFromTargetAndBoostSelfEffect()),
                "{X}{B/P}: Remove up to X counters from target permanent. For each counter removed this way, Hex Parasite gets +1/+0 until end of turn."
        ));
    }
}
