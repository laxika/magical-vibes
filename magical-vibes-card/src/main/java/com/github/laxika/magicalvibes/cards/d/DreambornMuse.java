package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "82")
public class DreambornMuse extends Card {

    public DreambornMuse() {
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                new MillEffect(new CardsInHand(CountScope.TARGET_PLAYER), MillRecipient.TARGET_PLAYER));
    }
}
