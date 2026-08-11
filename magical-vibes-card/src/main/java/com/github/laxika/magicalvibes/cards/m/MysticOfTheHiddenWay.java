package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;

@CardRegistration(set = "KTK", collectorNumber = "48")
public class MysticOfTheHiddenWay extends Card {

    public MysticOfTheHiddenWay() {
        addMorph("{2}{U}");
        addEffect(EffectSlot.STATIC, new CantBeBlockedEffect());
    }
}
