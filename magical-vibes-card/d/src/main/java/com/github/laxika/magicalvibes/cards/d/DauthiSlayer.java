package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "TMP", collectorNumber = "126")
@CardRegistration(set = "TPR", collectorNumber = "97")
@CardRegistration(set = "TSB", collectorNumber = "41")
public class DauthiSlayer extends Card {

    public DauthiSlayer() {
        addStaticEffect(new MustAttackEffect());
    }
}
