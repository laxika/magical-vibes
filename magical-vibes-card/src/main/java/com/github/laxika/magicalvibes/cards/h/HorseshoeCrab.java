package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.UntapSelfEffect;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "87")
public class HorseshoeCrab extends Card {

    public HorseshoeCrab() {
        addActivatedAbility(new ActivatedAbility(false, "{U}", List.of(new UntapSelfEffect()), false, "{U}: Untap Horseshoe Crab."));
    }
}
