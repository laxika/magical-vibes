package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

@CardRegistration(set = "THB", collectorNumber = "290")
public class SphinxMindbreaker extends Card {

    public SphinxMindbreaker() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MillEffect(10, MillRecipient.EACH_OPPONENT));
    }
}
