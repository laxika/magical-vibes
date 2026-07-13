package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "SHM", collectorNumber = "219")
public class TattermungeManiac extends Card {

    public TattermungeManiac() {
        addEffect(EffectSlot.STATIC, new MustAttackEffect());
    }
}
