package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

@CardRegistration(set = "PLS", collectorNumber = "130")
public class UrzasGuilt extends Card {

    public UrzasGuilt() {
        addEffect(EffectSlot.SPELL, new EachPlayerDrawsCardEffect(2));
        addEffect(EffectSlot.SPELL, new DiscardEffect(3, DiscardRecipient.EACH_PLAYER));
        addEffect(EffectSlot.SPELL, new LoseLifeEffect(4, LoseLifeRecipient.EACH_PLAYER));
    }
}
