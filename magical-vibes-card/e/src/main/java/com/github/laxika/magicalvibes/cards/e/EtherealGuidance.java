package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;

@CardRegistration(set = "SOI", collectorNumber = "18")
public class EtherealGuidance extends Card {

    public EtherealGuidance() {
        addSpellEffect(new BoostAllOwnCreaturesEffect(2, 1));
    }
}
