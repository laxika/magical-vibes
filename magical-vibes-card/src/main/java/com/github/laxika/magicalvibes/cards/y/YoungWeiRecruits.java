package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;

@CardRegistration(set = "PTK", collectorNumber = "94")
public class YoungWeiRecruits extends Card {

    public YoungWeiRecruits() {
        addEffect(EffectSlot.STATIC, new CantBlockEffect());
    }
}
