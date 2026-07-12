package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.NonbasicLandsBecomeTypeEffect;

@CardRegistration(set = "9ED", collectorNumber = "176")
@CardRegistration(set = "8ED", collectorNumber = "178")
public class BloodMoon extends Card {

    public BloodMoon() {
        addEffect(EffectSlot.STATIC, new NonbasicLandsBecomeTypeEffect(CardSubtype.MOUNTAIN));
    }
}
