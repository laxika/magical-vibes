package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

@CardRegistration(set = "JUD", collectorNumber = "68")
public class Guiltfeeder extends Card {

    public Guiltfeeder() {
        addEffect(EffectSlot.ON_ATTACKS_UNBLOCKED,
                new LoseLifeEffect(new CardsInGraveyard(null, CountScope.TARGET_PLAYER),
                        LoseLifeRecipient.TARGET_PLAYER));
    }
}
