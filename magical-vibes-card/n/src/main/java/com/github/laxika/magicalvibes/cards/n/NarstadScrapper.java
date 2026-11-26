package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;

@CardRegistration(set = "AVR", collectorNumber = "218")
public class NarstadScrapper extends Card {

    public NarstadScrapper() {
        addActivatedAbility(new ActivatedAbility(false, "{2}", List.of(BoostEffect.self(1, 0)), "{2}: This creature gets +1/+0 until end of turn."));
    }
}
