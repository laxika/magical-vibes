package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;

@CardRegistration(set = "IKO", collectorNumber = "79")
public class CavernWhisperer extends Card {

    public CavernWhisperer() {
        addEffect(EffectSlot.ON_SELF_MUTATES,
                new DiscardEffect(1, DiscardRecipient.EACH_OPPONENT));
    }
}
