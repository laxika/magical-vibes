package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;

@CardRegistration(set = "ONS", collectorNumber = "165")
public class ScreechingBuzzard extends Card {

    public ScreechingBuzzard() {
        addEffect(EffectSlot.ON_DEATH, new DiscardEffect(1, DiscardRecipient.EACH_OPPONENT));
    }
}
