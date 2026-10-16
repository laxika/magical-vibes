package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AmassGoblinsEffect;

@CardRegistration(set = "WAR", collectorNumber = "93")
public class HeraldOfTheDreadhorde extends Card {

    public HeraldOfTheDreadhorde() {
        addEffect(EffectSlot.ON_DEATH, new AmassGoblinsEffect(2, CardSubtype.ZOMBIE));
    }
}
