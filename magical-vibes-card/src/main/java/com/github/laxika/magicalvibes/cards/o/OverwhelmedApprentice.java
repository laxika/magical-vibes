package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "ELD", collectorNumber = "60")
public class OverwhelmedApprentice extends Card {

    public OverwhelmedApprentice() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MillEffect(2, MillRecipient.EACH_OPPONENT));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ScryEffect(2));
    }
}
