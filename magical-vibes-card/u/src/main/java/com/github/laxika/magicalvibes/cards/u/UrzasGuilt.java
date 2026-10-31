package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

@CardRegistration(set = "PLS", collectorNumber = "130")
public class UrzasGuilt extends Card {

    public UrzasGuilt() {
        addSpellEffect(new EachPlayerDrawsCardEffect(2));
        addSpellEffect(new DiscardEffect(3, DiscardRecipient.EACH_PLAYER));
        addSpellEffect(new LoseLifeEffect(4, LoseLifeRecipient.EACH_PLAYER));
    }
}
