package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "KTK", collectorNumber = "53")
public class ScionOfGlaciers extends Card {

    public ScionOfGlaciers() {
        addActivatedAbility(new ActivatedAbility(false, "{U}", List.of(new BoostSelfEffect(1, -1)), "{U}: This creature gets +1/-1 until end of turn."));
    }
}
