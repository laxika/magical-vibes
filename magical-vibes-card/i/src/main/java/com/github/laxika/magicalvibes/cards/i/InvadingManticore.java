package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AmassGoblinsEffect;

@CardRegistration(set = "WAR", collectorNumber = "134")
public class InvadingManticore extends Card {

    public InvadingManticore() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new AmassGoblinsEffect(2, CardSubtype.ZOMBIE));
    }
}
