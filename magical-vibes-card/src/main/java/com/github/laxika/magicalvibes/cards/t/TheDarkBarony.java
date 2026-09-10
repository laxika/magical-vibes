package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

@CardRegistration(set = "OHOP", collectorNumber = "6")
public class TheDarkBarony extends Card {

    public TheDarkBarony() {
        addEffect(EffectSlot.ON_NONBLACK_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE,
                new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PLAYER));
        addEffect(EffectSlot.CHAOS_TRIGGERED,
                new DiscardEffect(1, DiscardRecipient.EACH_OPPONENT));
    }
}
