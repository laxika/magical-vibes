package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

@CardRegistration(set = "APC", collectorNumber = "41")
public class LastCaress extends Card {

    public LastCaress() {
        addSpellEffect(new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PLAYER));
        addSpellEffect(new GainLifeEffect(1));
        addSpellEffect(new DrawCardEffect(1));
    }
}
