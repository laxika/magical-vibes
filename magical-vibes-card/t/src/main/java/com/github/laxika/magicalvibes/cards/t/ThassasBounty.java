package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

@CardRegistration(set = "THS", collectorNumber = "67")
public class ThassasBounty extends Card {

    public ThassasBounty() {
        addSpellEffect(new DrawCardEffect(3));
        addSpellEffect(new MillEffect(3, MillRecipient.TARGET_PLAYER));
    }
}
