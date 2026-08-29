package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "68")
public class Mistwalker extends Card {

    public Mistwalker() {
        addActivatedAbility(new ActivatedAbility(false, "{1}{U}", List.of(new BoostSelfEffect(1, -1)),
                "{1}{U}: This creature gets +1/-1 until end of turn."));
    }
}
