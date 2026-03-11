package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

import java.util.List;

@CardRegistration(set = "M11", collectorNumber = "203")
public class CrystalBall extends Card {

    public CrystalBall() {
        addActivatedAbility(new ActivatedAbility(true, "{1}", List.of(new ScryEffect(2)),
                "{1}, {T}: Scry 2."));
    }
}
