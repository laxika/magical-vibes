package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.ColorsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsForVividEffect;

@CardRegistration(set = "ECL", collectorNumber = "241")
public class SanarInnovativeFirstYear extends Card {

    public SanarInnovativeFirstYear() {
        addEffect(EffectSlot.PRECOMBAT_MAIN_TRIGGERED,
                new RevealTopCardsForVividEffect(new ColorsAmongControlledPermanents()));
    }
}
