package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "TMP", collectorNumber = "126")
@CardRegistration(set = "TPR", collectorNumber = "97")
public class DauthiSlayer extends Card {

    public DauthiSlayer() {
        addEffect(EffectSlot.STATIC, new MustAttackEffect());
    }
}
