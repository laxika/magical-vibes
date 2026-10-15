package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "HML", collectorNumber = "2a")
@CardRegistration(set = "HML", collectorNumber = "2b")
public class AbbeyMatron extends Card {

    public AbbeyMatron() {
        addActivatedAbility(new ActivatedAbility(true, "{W}", List.of(new BoostSelfEffect(0, 3)),
                "{W}, {T}: Abbey Matron gets +0/+3 until end of turn."));
    }
}
