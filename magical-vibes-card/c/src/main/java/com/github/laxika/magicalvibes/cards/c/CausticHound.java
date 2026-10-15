package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

@CardRegistration(set = "MBS", collectorNumber = "40")
public class CausticHound extends Card {

    public CausticHound() {
        addEffect(EffectSlot.ON_DEATH, new LoseLifeEffect(4, LoseLifeRecipient.EACH_PLAYER));
    }
}
