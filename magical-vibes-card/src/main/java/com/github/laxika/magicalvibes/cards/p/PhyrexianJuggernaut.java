package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "MBS", collectorNumber = "121")
public class PhyrexianJuggernaut extends Card {

    public PhyrexianJuggernaut() {
        addEffect(EffectSlot.STATIC, new MustAttackEffect());
    }
}
