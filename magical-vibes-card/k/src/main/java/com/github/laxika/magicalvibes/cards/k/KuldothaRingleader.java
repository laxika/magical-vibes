package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "MBS", collectorNumber = "70")
public class KuldothaRingleader extends Card {

    public KuldothaRingleader() {
        addEffect(EffectSlot.STATIC, new MustAttackEffect());
    }
}
