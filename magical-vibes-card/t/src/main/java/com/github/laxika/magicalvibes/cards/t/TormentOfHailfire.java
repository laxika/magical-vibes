package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TormentOfHailfireEffect;

@CardRegistration(set = "HOU", collectorNumber = "77")
@CardRegistration(set = "AKR", collectorNumber = "128")
@CardRegistration(set = "SLD", collectorNumber = "2287")
public class TormentOfHailfire extends Card {

    public TormentOfHailfire() {
        addSpellEffect(new TormentOfHailfireEffect(3).deferringChoicesEachIteration());
    }
}
