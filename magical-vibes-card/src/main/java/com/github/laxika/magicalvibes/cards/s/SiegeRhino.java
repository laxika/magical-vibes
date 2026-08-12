package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

@CardRegistration(set = "KTK", collectorNumber = "200")
public class SiegeRhino extends Card {

    public SiegeRhino() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new LoseLifeEffect(3, LoseLifeRecipient.EACH_OPPONENT));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifeEffect(3));
    }
}
