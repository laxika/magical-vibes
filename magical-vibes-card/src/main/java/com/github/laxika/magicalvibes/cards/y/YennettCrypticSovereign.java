package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardIfOddMayCastFreeElseDrawEffect;

@CardRegistration(set = "CMM", collectorNumber = "363")
@CardRegistration(set = "CMM", collectorNumber = "596")
public class YennettCrypticSovereign extends Card {

    public YennettCrypticSovereign() {
        addEffect(EffectSlot.ON_ATTACK, new RevealTopCardIfOddMayCastFreeElseDrawEffect());
    }
}
