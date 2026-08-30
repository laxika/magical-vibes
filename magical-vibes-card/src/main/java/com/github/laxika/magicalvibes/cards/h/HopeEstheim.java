package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.LifeGainedThisTurn;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

@CardRegistration(set = "FIN", collectorNumber = "226")
@CardRegistration(set = "FIN", collectorNumber = "396")
@CardRegistration(set = "FIN", collectorNumber = "491")
@CardRegistration(set = "FIN", collectorNumber = "541")
public class HopeEstheim extends Card {

    public HopeEstheim() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new MillEffect(new LifeGainedThisTurn(CountScope.CONTROLLER), MillRecipient.EACH_OPPONENT));
    }
}
