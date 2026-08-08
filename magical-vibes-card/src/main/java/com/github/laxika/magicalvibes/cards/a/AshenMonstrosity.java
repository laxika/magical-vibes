package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;

@CardRegistration(set = "BOK", collectorNumber = "93")
public class AshenMonstrosity extends Card {

    public AshenMonstrosity() {
        addEffect(EffectSlot.STATIC, new MustAttackEffect());
    }
}
