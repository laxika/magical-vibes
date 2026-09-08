package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "TMT", collectorNumber = "62")
public class DreamBeavers extends Card {

    public DreamBeavers() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifeEffect(1));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ScryEffect(1));
    }
}
