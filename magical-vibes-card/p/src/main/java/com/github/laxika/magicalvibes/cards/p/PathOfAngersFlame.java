package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;

@CardRegistration(set = "SOK", collectorNumber = "109")
public class PathOfAngersFlame extends Card {

    public PathOfAngersFlame() {
        addSpellEffect(BoostEffect.creaturesYouControl(2, 0));
    }
}
