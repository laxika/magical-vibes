package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOpponentPermanentOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardNamedIntoHandEffect;

@CardRegistration(set = "YMID", collectorNumber = "30")
public class PredatorySludge extends Card {

    public PredatorySludge() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ChooseOpponentPermanentOnEnterEffect(new ConjureCardNamedIntoHandEffect("YMID", "30", false)));
    }
}
