package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "181")
public class CavernCrawler extends Card {

    public CavernCrawler() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(BoostEffect.self(1, -1)),
                "{R}: This creature gets +1/-1 until end of turn."
        ));
    }
}
