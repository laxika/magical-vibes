package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsLifeEffect;

@CardRegistration(set = "MMQ", collectorNumber = "48")
public class SoothingBalm extends Card {

    public SoothingBalm() {
        addSpellEffect(new TargetPlayerGainsLifeEffect(5));
    }
}
