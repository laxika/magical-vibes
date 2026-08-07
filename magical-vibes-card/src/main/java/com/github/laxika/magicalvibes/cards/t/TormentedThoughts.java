package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

@CardRegistration(set = "ORI", collectorNumber = "122")
public class TormentedThoughts extends Card {

    public TormentedThoughts() {
        addEffect(EffectSlot.SPELL, new SacrificeCreatureCost(false, true));
        addEffect(EffectSlot.SPELL, new DiscardEffect(new XValue(), DiscardRecipient.TARGET_PLAYER));
    }
}
