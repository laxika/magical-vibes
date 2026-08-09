package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

@CardRegistration(set = "AKH", collectorNumber = "278")
@CardRegistration(set = "M19", collectorNumber = "295")
public class TatteredMummy extends Card {

    public TatteredMummy() {
        addEffect(EffectSlot.ON_DEATH, new LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT));
    }
}
