package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;

@CardRegistration(set = "8ED", collectorNumber = "205")
@CardRegistration(set = "9ED", collectorNumber = "205")
@CardRegistration(set = "P02", collectorNumber = "112")
public class OgreTaskmaster extends Card {

    public OgreTaskmaster() {
        addEffect(EffectSlot.STATIC, new CantBlockEffect());
    }
}
