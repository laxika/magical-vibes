package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfPerControlledSubtypeEffect;

@CardRegistration(set = "M11", collectorNumber = "134")
public class EarthServant extends Card {

    public EarthServant() {
        addEffect(EffectSlot.STATIC, new BoostSelfPerControlledSubtypeEffect(CardSubtype.MOUNTAIN, 0, 1));
    }
}
