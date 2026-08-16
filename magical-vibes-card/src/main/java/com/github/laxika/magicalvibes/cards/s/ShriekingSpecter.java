package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;

@CardRegistration(set = "S99", collectorNumber = "89")
public class ShriekingSpecter extends Card {

    public ShriekingSpecter() {
        addEffect(EffectSlot.ON_ATTACK,
                new DiscardEffect(1, DiscardRecipient.DEFENDING_PLAYER));
    }
}
