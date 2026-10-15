package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;

import java.util.List;

@CardRegistration(set = "RTR", collectorNumber = "29")
public class AquusSteed extends Card {

    public AquusSteed() {
        addActivatedAbility(new ActivatedAbility(true, "{2}{U}", List.of(new BoostTargetCreatureEffect(-2, 0)),
                "{2}{U}, {T}: Target creature gets -2/-0 until end of turn."));
    }
}
