package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBlockSourceEffect;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "198")
public class DuctCrawler extends Card {

    public DuctCrawler() {
        addActivatedAbility(new ActivatedAbility(false, "{1}{R}", List.of(new CantBlockSourceEffect(null)), true, "{1}{R}: Target creature can't block Duct Crawler this turn."));
    }
}
