package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AmassGoblinsEffect;

@CardRegistration(set = "WAR", collectorNumber = "42")
public class AvenEternal extends Card {

    public AvenEternal() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new AmassGoblinsEffect(1, CardSubtype.ZOMBIE));
    }
}