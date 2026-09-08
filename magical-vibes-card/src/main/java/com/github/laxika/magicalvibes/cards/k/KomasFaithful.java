package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

@CardRegistration(set = "KHM", collectorNumber = "102")
public class KomasFaithful extends Card {

    public KomasFaithful() {
        addEffect(EffectSlot.ON_DEATH, new MillEffect(3, MillRecipient.CONTROLLER));
        addEffect(EffectSlot.ON_DEATH, new MillEffect(3, MillRecipient.EACH_OPPONENT));
    }
}
