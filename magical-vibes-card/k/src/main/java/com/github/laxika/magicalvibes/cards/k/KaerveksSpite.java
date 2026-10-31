package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardHandCost;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificeAllPermanentsYouControlCost;

@CardRegistration(set = "VIS", collectorNumber = "63")
public class KaerveksSpite extends Card {

    public KaerveksSpite() {
        // As an additional cost to cast this spell, sacrifice all permanents you control and discard your hand.
        addCastingCost(new SacrificeAllPermanentsYouControlCost());
        addCastingCost(new DiscardHandCost());
        // Target player loses 5 life.
        addSpellEffect(new LoseLifeEffect(5, LoseLifeRecipient.TARGET_PLAYER));
    }
}
