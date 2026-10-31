package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "M10", collectorNumber = "128")
public class BurningInquiry extends Card {

    public BurningInquiry() {
        addSpellEffect(new EachPlayerDrawsCardEffect(3));
        addSpellEffect(new DiscardEffect(3, DiscardRecipient.EACH_PLAYER, true));
    }
}
