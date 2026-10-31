package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "BNG", collectorNumber = "65")
@CardRegistration(set = "2XM", collectorNumber = "91")
@CardRegistration(set = "CMM", collectorNumber = "154")
public class DrownInSorrow extends Card {

    public DrownInSorrow() {
        addSpellEffect(new BoostAllCreaturesEffect(-2, -2));
        addSpellEffect(new ScryEffect(1));
    }
}
