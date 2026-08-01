package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardHandCost;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificeAllPermanentsYouControlCost;

@CardRegistration(set = "VIS", collectorNumber = "63")
public class KaerveksSpite extends Card {

    public KaerveksSpite() {
        // As an additional cost to cast this spell, sacrifice all permanents you control and discard your hand.
        addEffect(EffectSlot.SPELL, new SacrificeAllPermanentsYouControlCost());
        addEffect(EffectSlot.SPELL, new DiscardHandCost());
        // Target player loses 5 life.
        addEffect(EffectSlot.SPELL, new LoseLifeEffect(5, LoseLifeRecipient.TARGET_PLAYER));
    }
}
