package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;

@CardRegistration(set = "HML", collectorNumber = "2a")
@CardRegistration(set = "HML", collectorNumber = "2b")
public class AbbeyMatron extends Card {

    public AbbeyMatron() {
        addActivatedAbility(new ActivatedAbility(true, "{W}", List.of(BoostEffect.self(0, 3)),
                "{W}, {T}: Abbey Matron gets +0/+3 until end of turn."));
    }
}
