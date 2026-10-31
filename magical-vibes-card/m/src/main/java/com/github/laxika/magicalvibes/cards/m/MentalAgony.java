package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

@CardRegistration(set = "AVR", collectorNumber = "114")
public class MentalAgony extends Card {

    public MentalAgony() {
        // Target player discards two cards and loses 2 life; both effects act on the same targeted player.
        addSpellEffect(new DiscardEffect(2, DiscardRecipient.TARGET_PLAYER));
        addSpellEffect(new LoseLifeEffect(2, LoseLifeRecipient.TARGET_PLAYER));
    }
}
