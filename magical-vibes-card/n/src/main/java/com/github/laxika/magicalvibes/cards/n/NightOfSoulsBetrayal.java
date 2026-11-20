package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "CHK", collectorNumber = "133")
@CardRegistration(set = "IMA", collectorNumber = "99")
@CardRegistration(set = "HA6", collectorNumber = "4")
public class NightOfSoulsBetrayal extends Card {

    public NightOfSoulsBetrayal() {
        addStaticEffect(new StaticBoostEffect(-1, -1, Subjects.allCreatures()));
    }
}
