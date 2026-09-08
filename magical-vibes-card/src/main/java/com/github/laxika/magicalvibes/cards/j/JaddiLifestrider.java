package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TapCreaturesGainLifePerCreatureEffect;

@CardRegistration(set = "ROE", collectorNumber = "189")
public class JaddiLifestrider extends Card {

    public JaddiLifestrider() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new TapCreaturesGainLifePerCreatureEffect(2));
    }
}
