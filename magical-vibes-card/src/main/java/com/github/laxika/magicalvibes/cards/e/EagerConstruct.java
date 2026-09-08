package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayScryEffect;

@CardRegistration(set = "KLD", collectorNumber = "209")
public class EagerConstruct extends Card {

    public EagerConstruct() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EachPlayerMayScryEffect(1));
    }
}
