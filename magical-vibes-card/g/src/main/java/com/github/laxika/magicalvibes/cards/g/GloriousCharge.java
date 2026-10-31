package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "M10", collectorNumber = "11")
@CardRegistration(set = "M13", collectorNumber = "15")
public class GloriousCharge extends Card {

    public GloriousCharge() {
        addSpellEffect(new BoostAllOwnCreaturesEffect(1, 1));
    }
}
