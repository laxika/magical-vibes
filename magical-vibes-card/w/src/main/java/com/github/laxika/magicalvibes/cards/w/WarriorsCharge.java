package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "POR", collectorNumber = "38")
public class WarriorsCharge extends Card {

    public WarriorsCharge() {
        addSpellEffect(new BoostAllOwnCreaturesEffect(1, 1));
    }
}
